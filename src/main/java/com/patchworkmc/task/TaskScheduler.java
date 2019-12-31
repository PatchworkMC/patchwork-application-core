/*
 * Patchwork Project
 * Copyright (C) 2019 PatchworkMC and contributors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.patchworkmc.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.patchworkmc.logging.LogLevel;
import com.patchworkmc.logging.Logger;

/**
 * Task scheduler for asynchronously running {@link Task}s.
 */
public class TaskScheduler {
	private final Logger logger;
	private final int cores;

	private final ThreadGroup threadGroup;
	private final Thread[] threads;

	// Atomic flag to signal wether the task scheduler
	// should be shut down
	private final AtomicBoolean shutdown;

	private final List<Task> tasks;
	private final Object lock;

	// Used for keeping track of how many task runners
	// are still running
	private final CountDownLatch countDownLatch;

	/**
	 * Creates a new {@link TaskScheduler}.
	 *
	 * @param logger The logger to use within the scheduler, will also be
	 *               used for deriving loggers for the runners which in
	 *               return will be derived for the tasks
	 * @param cores  The number of CPU cores to use, in other words, the number
	 *               of runners
	 */
	public TaskScheduler(Logger logger, int cores) {
		this.logger = logger;
		this.cores = cores;

		threadGroup = new RestartingThreadGroup("Task Threads");
		threads = new Thread[cores];

		shutdown = new AtomicBoolean(false);

		tasks = new ArrayList<>();
		lock = new Object();

		countDownLatch = new CountDownLatch(cores);
	}

	/**
	 * Starts the task scheduler and makes it process
	 * queued tasks.
	 */
	public void start() {
		logger.info("Starting scheduler");
		shutdown.set(false);

		for (int i = 0; i < cores; i++) {
			threads[i] = new RunnerThread(threadGroup, logger.sub("Task runner " + (i + 1)), i);
			threads[i].start();
		}

		logger.info("Done!");
	}

	/**
	 * Signals the task scheduler to shut down. This method
	 * returns immediately, see {@link TaskScheduler#awaitShutdown(long, TimeUnit)}
	 * on a way to block until the scheduler has stopped.
	 */
	public void shutdown() {
		logger.info("Requesting scheduler stop.");
		shutdown.set(true);
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	/**
	 * Retrieves if the scheduler's shutdown flag is set.
	 *
	 * @return True if the schedulers shutdown flag is set, false otherwise
	 */
	public boolean isShuttingDown() {
		return shutdown.get();
	}

	/**
	 * Retrieves if the scheduler is running.
	 *
	 * @return True if the scheduler is running, false otherwise
	 */
	public boolean isRunning() {
		return threadGroup.activeCount() > 0;
	}

	/**
	 * Forces the scheduler to shutdown. This method can only be called
	 * after {@link TaskScheduler#shutdown()} has been called.
	 */
	public void forceShutdown() {
		if (!isRunning()) {
			return;
		}

		if (!isShuttingDown()) {
			// Only allow a forceful shutdown, if there was a chance to shut down
			// gracefully
			throw new IllegalStateException("Tried to forcefully shutdown scheduler without requesting normal shutdown");
		}

		logger.warn("Forcefully stopping scheduler...");

		// Interrupt all threads
		threadGroup.interrupt();

		try {
			// Give the threads 2 seconds time to shut down
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			logger.error("Failed to wait 2 seconds!");
			logger.thrown(LogLevel.ERROR, e);
		}

		if (isRunning()) {
			logger.warn("Failed to forcefully stop task runners with interrupt, forcing via stop()!");

			for (Thread t : threads) {
				// This method is deprecated for a reason... it should kill the threads
				// regardless of what it is doing
				t.stop();
			}

			// Sometimes the JVM decides to just not stop a thread. There is nothing we
			// can do against it other than just logging it and carrying on
			if (isRunning()) {
				logger.error("Failed to stop task scheduler even with stop()!");
			}
		} else {
			logger.info("Done!");
		}
	}

	/**
	 * Schedules a new task to run later.
	 *
	 * @param t The task to schedule
	 * @return The tracker tracking this task
	 */
	public TaskTracker schedule(Task t) {
		TaskTracker tracker = new TaskTracker(this);
		schedule(t, tracker, false);
		return tracker;
	}

	/**
	 * Schedules a new task to run later.
	 *
	 * @param t              The task to schedule
	 * @param trackOnCurrent If true and called from a runner thread, the newly scheduled task
	 *                       will be tracked by all trackers tracking the currently running task
	 *                       on the thread
	 */
	public void schedule(Task t, boolean trackOnCurrent) {
		schedule(t, null, trackOnCurrent);
	}

	/**
	 * Schedules a new task to run later.
	 *
	 * @param t       The task to schedule
	 * @param tracker The tracker the task should be tracker by or null, to not track the
	 *                task
	 * @return tracker
	 */
	public TaskTracker schedule(Task t, TaskTracker tracker) {
		schedule(t, null, false);
		return tracker;
	}

	/**
	 * Schedules a new task to run later.
	 *
	 * @param t              The task to schedule
	 * @param tracker        The tracker the task should be tracker by or null, to not track the
	 *                       task
	 * @param trackOnCurrent If true and called from a runner thread, the newly scheduled task
	 *                       will be tracked by all trackers tracking the currently running task
	 *                       on the thread
	 */
	public void schedule(Task t, TaskTracker tracker, boolean trackOnCurrent) {
		if (shutdown.get()) {
			logger.warn("Not scheduling Task %s because scheduler is already shutting down!", t.name());
		}

		if (tracker != null) {
			// If the tracker is not null, begin tracking the new task
			tracker.track(t);
		}

		if (trackOnCurrent) {
			Thread currentThread = Thread.currentThread();

			if (currentThread instanceof RunnerThread) {
				// Track the new task on all trackers tracking the currently executing task
				((RunnerThread) currentThread).task.trackers().forEach(currentTracker -> currentTracker.track(t));
			}
		}

		// Synchronize on lock and schedule the task
		synchronized (lock) {
			tasks.add(t);
			lock.notifyAll();
		}
	}

	/**
	 * Class of all tasks runner threads. Holds thread local information.
	 */
	private class RunnerThread extends Thread {
		private final Logger logger;
		private final int runnerIndex;
		private Task task;

		/**
		 * Creates a new {@link RunnerThread}.
		 *
		 * @param threadGroup The thread group this runner belongs to
		 * @param logger      The logger this runner should use for logging and
		 *                    deriving loggers for the tasks it is running
		 * @param runnerIndex The index of the runner, used to identify it
		 */
		RunnerThread(ThreadGroup threadGroup, Logger logger, int runnerIndex) {
			super(threadGroup, "TaskSchedulerRunner" + runnerIndex);
			this.logger = logger;
			this.runnerIndex = runnerIndex;
		}

		@Override
		public void run() {
			logger.debug("Runner started.");

			// Continue running as long as tasks are queued
			// or the shutdown flag has not been set
			while (!shutdown.get() || !tasks.isEmpty()) {
				// Synchronize on the scheduler's lock
				synchronized (lock) {
					try {
						// Try to find an available task
						Optional<Task> optionalTask = tasks.stream().findAny();

						if (!optionalTask.isPresent()) {
							// No task found, so wait on the lock
							// if a new task gets scheduled, the lock gets notified
							lock.wait();
							continue;
						}

						task = optionalTask.get();
					} catch (InterruptedException e) {
						// The thread was interrupted, this might occur for multiple reasons
						// one of them is, that the scheduler's shutdown flag has been set, so
						// test for it and break out of the loop if so
						if (shutdown.get()) {
							break;
						} else {
							// In case the shutdown flag has not been set,
							// a spurious wakeup might have occurred. Log that and then
							// continue as if nothing happened-
							logger.error("Failed to wait on lock!");
							logger.thrown(LogLevel.ERROR, e);
						}

						continue;
					}

					// Remove the task so no other runner can pick it up
					tasks.remove(task);
				}

				// If the task has been cancelled, don't try to execute it
				if (task.isCanceled()) {
					continue;
				}

				try {
					// Step the task by one
					task.step(TaskScheduler.this, logger);

					if (!task.isDone()) {
						// If the task is not done yet, reschedule it
						synchronized (lock) {
							tasks.add(task);
						}
					}
				} catch (Throwable t) {
					logger.error("Task %s failed!", task.name());
					logger.thrown(LogLevel.ERROR, t);
				}

				// This runner no longer owns a task
				task = null;
			}

			logger.debug("Runner terminating.");

			// Make sure all other runners get notified too
			synchronized (lock) {
				lock.notifyAll();
			}

			// Decrease the amount of runners running so the
			// scheduler can shutdown, if this was the last runner
			countDownLatch.countDown();
		}
	}

	/**
	 * Thread group used for all runner threads. This thread group
	 * automatically restarts crashed runner threads.
	 */
	private class RestartingThreadGroup extends ThreadGroup {
		RestartingThreadGroup(String name) {
			super(name);
		}

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			if (!(t instanceof RunnerThread)) {
				// This might occur if a thread has been launched from within
				// a runner. This is not our business, so delegate the handling
				// of the failure
				super.uncaughtException(t, e);
				return;
			}

			logger.warn("Task runner crashed, trying to start again...");
			int indexOfCrashed = ((RunnerThread) t).runnerIndex;

			// Restart the task runner by its index
			threads[indexOfCrashed] =
					new RunnerThread(threadGroup, logger.sub("Task runner " + (indexOfCrashed + 1)), indexOfCrashed);
			threads[indexOfCrashed].start();
			logger.info("Task runner restarted.");
		}
	}

	/**
	 * Blocks for maximal specified amount of time until the task scheduler
	 * has shut down.
	 *
	 * @param timeout The time to wait for without any specific unit
	 * @param unit    The unit of the time specified
	 * @return True if the task scheduler has stopped, false otherwise
	 * @throws InterruptedException If the tread gets interrupted while waiting
	 */
	public boolean awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException {
		return countDownLatch.await(timeout, unit);
	}
}

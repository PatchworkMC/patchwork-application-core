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
import java.util.Collections;
import java.util.List;

import com.patchworkmc.logging.Logger;

/**
 * Base class for tasks that may asynchronously run on a {@link TaskScheduler}.
 */
public abstract class Task {
	/**
	 * The code the task executes.
	 *
	 * @param logger The logger the task should use for task operation logging.
	 * @return {@code true} if the task is done, false if it should be rescheduled.
	 * This allows tasks to be stateful and run multiple times without blocking
	 * any task runner inbetween.
	 * @throws Throwable If an error in the task occurs. A task that has thrown an error
	 *                   will be marked as failed with the error thrown and fail every
	 *                   task operation it belongs to
	 */
	protected abstract boolean run(Logger logger) throws Throwable;

	/**
	 * Retrieves a human readable name of the task.
	 *
	 * @return A human readable name
	 */
	public abstract String name();

	// Keep track of which task trackers we are tracked by
	private final List<TaskTracker> trackedBy;
	private TaskScheduler scheduler;

	public Task() {
		trackedBy = new ArrayList<>();
	}

	// Task state store
	private volatile boolean done;
	private volatile boolean running;
	private Throwable error;
	private boolean canceled;

	/**
	 * Runs one task iteration.
	 *
	 * @param scheduler The task scheduler executing the step
	 * @param logger    The logger to use, will be adjusted automatically before
	 *                  being passed to the task's run method.
	 * @throws Throwable If the task throws anything
	 */
	final synchronized void step(TaskScheduler scheduler, Logger logger) throws Throwable {
		if (this.scheduler == null) {
			this.scheduler = scheduler;
		} else if (this.scheduler != scheduler) {
			throw new IllegalArgumentException("Task has already run on a scheduler which is a different one");
		}

		running = true;

		try {
			done = run(logger.sub("Task(" + name() + ")"));
		} catch (Throwable t) {
			// Set state on error
			running = false;
			error = t;
			done = true;

			// Inform trackers we failed
			synchronized (trackedBy) {
				trackedBy.forEach(tracker -> tracker.notifyFailed(this, error));
				trackedBy.clear();
			}

			throw t;
		}

		if (done) {
			// Inform trackers we succeeded
			synchronized (trackedBy) {
				trackedBy.forEach(tracker -> tracker.notifyDone(this));
				trackedBy.clear();
			}
		}

		running = false;
	}

	/**
	 * Checks if task is done.
	 *
	 * @return {@code true} if the task is done, {@code false} otherwise
	 */
	public final boolean isDone() {
		return done;
	}

	/**
	 * Checks if the task is currently running.
	 * <b>This might is false during the phases the task is waiting
	 * in the scheduler queue</b>. This means, the value may change
	 * multiple times.
	 *
	 * @return If the task is currently running on any task runner
	 */
	public final boolean isRunning() {
		return running;
	}

	/**
	 * Retrieves the error which occurred in the task, if any.
	 *
	 * @return The error which occurred in the task if any, null if no error occurred
	 */
	public synchronized Throwable getError() {
		return error;
	}

	/**
	 * Sets the task's canceled flag. If the task is currently running,
	 * it will not be aborted! It will just never be rescheduled. It also
	 * informs all trackers that this task is done.
	 */
	public final void cancel() {
		canceled = true;
		synchronized (trackedBy) {
			trackedBy.forEach(tracker -> tracker.notifyDone(this));
		}
	}

	/**
	 * Checks wether the task has been canceled.
	 *
	 * @return True if the task has been canceled, false otherwise
	 */
	public final boolean isCanceled() {
		return canceled;
	}

	/**
	 * Retrieves the scheduler this task is running on.
	 *
	 * @return The scheduler this task is running on
	 */
	public final TaskScheduler getScheduler() {
		return scheduler;
	}

	/**
	 * Retrieves an immutable list of trackers currently tracking this task.
	 *
	 * @return A list of trackers currently tracking this task
	 */
	public List<TaskTracker> trackers() {
		synchronized (trackedBy) {
			return Collections.unmodifiableList(trackedBy);
		}
	}

	/**
	 * Informs this task it is now being also tracked by the specified tracker.
	 *
	 * @param tracker The tracker now also tracking this task
	 */
	void nowTrackedBy(TaskTracker tracker) {
		synchronized (trackedBy) {
			if (!trackedBy.contains(tracker)) {
				trackedBy.add(tracker);
			}
		}
	}
}

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

import com.patchworkmc.logging.Logger;
import com.patchworkmc.function.ThrowingConsumer;
import com.patchworkmc.function.ThrowingRunnable;

// Please note, this is class is heavily synchronized with a lot of
// separated blocks. This design decision has been made to minimize
// possible deadlocks and make the execution faster. On a modern
// computer resources are not too limited, so having a lot of locks
// is fine.

/**
 * Tracker for tasks which have been submitted to a {@link TaskScheduler}.
 */
public class TaskTracker {
	/**
	 * Helper class storing callbacks an their associated {@link TaskTracker}.
	 */
	static class TaskTrackerListener {
		private final ThrowingRunnable<Throwable> onSuccess;
		private final ThrowingConsumer<Throwable, Throwable> onFail;
		private final TaskTracker newTracker;

		/**
		 * Creates a new {@link TaskTrackerListener}.
		 *
		 * @param onSuccess  The callback to execute in case all tracked
		 *                   tasks succeeded
		 * @param onFail     The callback to execute in case a tracked
		 *                   task failed
		 * @param newTracker The tracker to execute these callbacks on
		 */
		TaskTrackerListener(
				ThrowingRunnable<Throwable> onSuccess,
				ThrowingConsumer<Throwable, Throwable> onFail,
				TaskTracker newTracker
		) {
			this.onSuccess = onSuccess;
			this.onFail = onFail;
			this.newTracker = newTracker;
		}
	}

	/**
	 * Dummy task for connection {@link TaskTracker}s with each other.
	 */
	static class DummyTask extends Task {
		@Override
		protected boolean run(Logger logger) {
			throw new UnsupportedOperationException("The dummy task cannot be run");
		}

		@Override
		public String name() {
			return "DummyTask";
		}
	}

	// Things the tracker requires
	private final TaskScheduler scheduler;
	private final List<Task> trackedTasks;
	private final List<TaskTrackerListener> listeners;
	private final DummyTask dummyTask;
	private final List<TaskTracker> trackedBy;

	// Tracker state
	private boolean done; // synchronized via the listeners field
	private volatile boolean armed;
	private Throwable error;

	/**
	 * Creates a new {@link TaskTracker} associated with a {@link TaskScheduler}.
	 *
	 * @param scheduler The task scheduler tasks tracked by this tracker are from
	 */
	public TaskTracker(TaskScheduler scheduler) {
		this.scheduler = scheduler;
		this.trackedTasks = new ArrayList<>();
		this.listeners = new ArrayList<>();
		this.dummyTask = new DummyTask();
		this.trackedBy = new ArrayList<>();
	}

	/**
	 * Notifies the tracker that one of its tracked tasks failed.
	 *
	 * @param task  The task that failed
	 * @param error The exception the task failed with
	 */
	public void notifyFailed(Task task, Throwable error) {
		if (done) {
			throw new IllegalStateException("Tried to notify TaskTracker which is done already");
		}

		synchronized (trackedTasks) {
			// Check if the task even belongs to this tracker
			if (trackedTasks.contains(task)) {
				this.error = error;
				trackedTasks.remove(task);

				// Check if it was the last task
				checkListeners();
			}
		}
	}

	/**
	 * Notifies the tracker that one if its tracked tasks succeeded.
	 *
	 * @param task The task that succeeded
	 */
	public void notifyDone(Task task) {
		if (done) {
			throw new IllegalStateException("Tried to notify TaskTracker which is done already");
		}

		synchronized (trackedTasks) {
			// Check if the task even belongs to this tracker
			if (trackedTasks.contains(task)) {
				trackedTasks.remove(task);

				// Check if it was the last task
				checkListeners();
			}
		}
	}

	/**
	 * This function checks if there are no tasks left, and if so,
	 * takes the appropriate actions.
	 */
	private void checkListeners() {
		// Check if the tracker has been armed, if not,
		// don't do anything
		if (!armed) {
			return;
		}

		synchronized (trackedTasks) {
			if (trackedTasks.size() == 0) {
				synchronized (listeners) {
					// The tracker is done since all tasks of it have run
					done = true;

					listeners.forEach(this::fireListener);
				}
			}
		}
	}

	/**
	 * Fires a tracker listener according to the state of this tracker.
	 *
	 * @param listener The listener to fire
	 */
	private void fireListener(TaskTrackerListener listener) {
		synchronized (listener) {
			if (listener.newTracker.done) {
				// This should never happen, since we have a dummy task blocking
				// the other tracker
				throw new IllegalStateException("Dependent tracker is done already");
			}

			if (error == null) {
				// All tasks completed successfully
				if (listener.onSuccess != null) {
					// If the callback is not null, schedule it
					scheduler.schedule(
							new UniversalRunnableTask("SuccessListener", listener.onSuccess), listener.newTracker);
				}

				// Notify that the dummy task is done, so we no longer
				// block the other tracker with it
				listener.newTracker.notifyDone(dummyTask);
			} else {
				if (listener.onFail != null) {
					// If the callback is not null, schedule it
					scheduler.schedule(
							new UniversalRunnableTask("SuccessListener",
									(ThrowingRunnable<Throwable>) () -> listener.onFail.accept(error)),
							listener.newTracker);
					// Succeed the dummy task, it will be up to the callback
					// to fail if the chain should be failed further
					listener.newTracker.notifyDone(dummyTask);
				} else {
					// No callback has been supplied so fail the
					// other tracker with our error
					listener.newTracker.notifyFailed(dummyTask, error);
				}
			}
		}
	}

	/**
	 * Add a task to track, this tracker will then only complete when the
	 * supplied task has also completed. If the supplied tasks fails, this
	 * tracker also fails.
	 *
	 * @param toTrack The task to be tracked
	 * @return this
	 */
	public TaskTracker track(Task toTrack) {
		if (done) {
			throw new IllegalStateException("Tried to track new task with TaskTracker which was done already");
		}

		if (!trackedTasks.contains(toTrack) && !toTrack.isDone()) {
			synchronized (trackedTasks) {
				toTrack.nowTrackedBy(this);
				trackedTasks.add(toTrack);
			}

			synchronized (trackedBy) {
				trackedBy.forEach(tracker -> tracker.track(toTrack));
			}
		}

		return this;
	}

	/**
	 * Adds a tracker to track. This essentially means, that all tracked tasks
	 * are inherited from the tracker.
	 *
	 * @param tracker The tracker to inherit tasks from
	 * @return this
	 */
	public TaskTracker track(TaskTracker tracker) {
		synchronized (tracker.trackedBy) {
			tracker.trackedBy.add(this);
		}

		synchronized (tracker.trackedTasks) {
			tracker.trackedTasks.forEach(this::track);
		}

		return this;
	}

	/**
	 * Arms this tracker and thus makes it fire, as soon as
	 * all tasks have finished, which my be directly when calling
	 * this method.
	 *
	 * @return this
	 */
	public TaskTracker arm() {
		armed = true;
		checkListeners();
		return this;
	}

	/**
	 * Chains an operation which should be executed when this tracker
	 * completed. If the tracker has completed already, the listener
	 * is scheduled immediately.
	 *
	 * @param listener The listener to execute when this tracker completed
	 * @return The new tracker of the operation, it is armed already
	 */
	public TaskTracker then(ThrowingRunnable<Throwable> listener) {
		return then(listener, false);
	}

	/**
	 * Chains an operation which should be executed when this tracker
	 * completed. If the tracker has completed already, the listener
	 * is scheduled immediately.
	 *
	 * @param listener The listener to execute when this tracker completed
	 * @param noArm    If true, the returned tracker will not be armed
	 * @return The new tracker of the operation, armed already when noArm is false
	 */
	public TaskTracker then(ThrowingRunnable<Throwable> listener, boolean noArm) {
		TaskTracker tracker = new TaskTracker(scheduler);
		tracker.track(dummyTask);

		TaskTrackerListener trackerListener = new TaskTrackerListener(listener, null, tracker);

		// Synchronize on listeners
		synchronized (listeners) {
			if (done) {
				// if done, fire the listener right now
				fireListener(trackerListener);
			} else {
				// else schedule it for later
				listeners.add(trackerListener);
			}
		}

		// Arm the tracker of noArm was false
		if (!noArm) {
			tracker.arm();
		}

		return tracker;
	}

	/**
	 * Chains an operation which should be executed when this tracker
	 * failed. If the tracker has failed already, the listener
	 * is scheduled immediately.
	 *
	 * @param listener The listener to execute when this tracker failed
	 * @return The new tracker of the operation, it is armed already
	 */
	public TaskTracker except(ThrowingConsumer<Throwable, Throwable> listener) {
		return except(listener, false);
	}

	/**
	 * Chains an operation which should be executed when this tracker
	 * failed. If the tracker has failed already, the listener
	 * is scheduled immediately.
	 *
	 * @param listener The listener to execute when this tracker failed
	 * @param noArm    If true, the returned tracker will not be armed
	 * @return The new tracker of the operation, armed already when noArm is false
	 */
	public TaskTracker except(ThrowingConsumer<Throwable, Throwable> listener, boolean noArm) {
		TaskTracker tracker = new TaskTracker(scheduler);
		tracker.track(dummyTask);

		TaskTrackerListener trackerListener = new TaskTrackerListener(null, listener, tracker);

		// Synchronize on listeners
		synchronized (listeners) {
			if (done) {
				// if done, fire the listener right now
				fireListener(trackerListener);
			} else {
				// else schedule it for later
				listeners.add(trackerListener);
			}
		}

		// Arm the tracker of noArm was false
		if (!noArm) {
			tracker.arm();
		}

		return tracker;
	}
}

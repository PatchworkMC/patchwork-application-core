package com.patchworkmc.task;

import com.patchworkmc.logging.Logger;
import com.patchworkmc.util.IThrowingRunnable;

/**
 * {@link Task} for {@link Runnable}s and {@link IThrowingRunnable}s.
 */
public class UniversalRunnableTask extends Task {
	private final String name;
	private final IThrowingRunnable<Throwable> runnable;

	/**
	 * Creates a new {@link UniversalRunnableTask} with the specified name and runnable.
	 *
	 * @param name     The name of the task
	 * @param runnable The runnable to run as the task itself
	 */
	public UniversalRunnableTask(String name, IThrowingRunnable<Throwable> runnable) {
		this.name = name;
		this.runnable = runnable;
	}

	/**
	 * Creates a new {@link UniversalRunnableTask} with the specified name and runnable.
	 *
	 * @param name     The name of the task
	 * @param runnable The runnable to run as the task itself
	 */
	public UniversalRunnableTask(String name, Runnable runnable) {
		this.name = name;
		// We can't directly assign the runnable, but we can make a
		// method reference out of it
		this.runnable = runnable::run;
	}

	@Override
	protected boolean run(Logger logger) throws Throwable {
		logger.debug("Running universal runnable.");
		runnable.run();
		return true;
	}

	@Override
	public String name() {
		return name;
	}
}

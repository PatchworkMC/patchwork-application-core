package com.patchworkmc.util;

/**
 * Rough reimplementation of {@link Runnable}. This does not try to provide a 100% replacement,
 * it is just used in tasks.
 *
 * @param <E> The error type that may occur while running
 */
@FunctionalInterface
public interface IThrowingRunnable<E extends Throwable> {
	/**
	 * Runs the runnable.
	 *
	 * @throws E If the runnable throws
	 */
	void run() throws E;
}

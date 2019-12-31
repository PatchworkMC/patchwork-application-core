package com.patchworkmc.util;

/**
 * Rough reimplementation of {@link java.util.function.Consumer}. This does not try to provide a 100% replacement,
 * it is just used in tasks.
 *
 * @param <T> The type this consumer accepts
 * @param <E> The error type that may occur while accepting a value
 */
@FunctionalInterface
public interface IThrowingConsumer<T, E extends Throwable> {
	/**
	 * Sends a value to the consumer.
	 *
	 * @param t The value to send
	 * @throws E If the consumer throws
	 */
	void accept(T t) throws E;
}

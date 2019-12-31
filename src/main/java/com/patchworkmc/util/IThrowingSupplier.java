package com.patchworkmc.util;

/**
 * Rough reimplementation of {@link java.util.function.Supplier}. This does not try to provide a 100% replacement,
 * it is just used in tasks.
 *
 * @param <T> The type this supplier yields
 * @param <E> The error type that may occur while yielding a value
 */
@FunctionalInterface
public interface IThrowingSupplier<T, E extends Throwable> {
	/**
	 * Requests a value from the supplier.
	 *
	 * @return The value the supplier yielded
	 * @throws E If the supplier throws
	 */
	T get() throws E;
}

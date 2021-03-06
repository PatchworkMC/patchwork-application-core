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

package com.patchworkmc.function;

/**
 * Rough reimplementation of {@link java.util.function.Consumer}. This does not try to provide a 100% replacement,
 * it is just used in tasks.
 *
 * @param <T> The type this consumer accepts
 * @param <E> The error type that may occur while accepting a value
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
	/**
	 * Sends a value to the consumer.
	 *
	 * @param t The value to send
	 * @throws E If the consumer throws
	 */
	void accept(T t) throws E;
}

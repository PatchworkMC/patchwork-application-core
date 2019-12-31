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

package com.patchworkmc.commandline;

import java.lang.reflect.Field;

/**
 * Interface representing a type which can map strings (or null) to other types.
 */
public interface TypeMapper {
	/**
	 * Tries to apply the given value to the target. What exactly this does, is implementation
	 * defined.
	 *
	 * @param value The value which should be applied, will be null if {@link
	 *              TypeMapper#acceptsValue()} is false
	 * @throws CommandlineParseException If value could not be converted into the target type
	 * @throws CommandlineException      If something goes wrong while setting the field
	 */
	void apply(String value) throws CommandlineException;

	/**
	 * Determines wether a value is accepted and required.
	 *
	 * @return {@code true} if a value is accepted and required, {@code false} otherwise.
	 */
	boolean acceptsValue();

	/**
	 * Determines wether this type mapper accepts more {@link TypeMapper#apply(String)} calls.
	 *
	 * @return Wether more apply calls are accepted
	 */
	boolean filled();

	// Helper interface since BiFunction's can't throw
	@FunctionalInterface
	interface TypeMapperFactory {
		TypeMapper create(Object target, Field f) throws Throwable;
	}

	// Fake "null" class since annotations can't return null be default
	abstract class NullTypeMapper implements TypeMapper {
	}
}

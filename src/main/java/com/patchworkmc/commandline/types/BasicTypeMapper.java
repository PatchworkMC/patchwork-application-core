/*
 * Patchwork Project
 * Copyright (c) 2016-2019, 2019
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.patchworkmc.commandline.types;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Field;

import com.patchworkmc.commandline.CommandlineException;
import com.patchworkmc.commandline.CommandlineParser;
import com.patchworkmc.commandline.TypeMapper;

/**
 * Base class for creating simple {@link TypeMapper}s usable by the {@link CommandlineParser}.
 *
 * @param <T> The target type this class can map {@link String}s to
 */
public abstract class BasicTypeMapper<T> implements TypeMapper {
	// Keep a MethodHandle for faster reflection
	private final MethodHandle setter;

	// A BasicTypeMapper assumes it has a onetime set field which is fully filled after being set
	// once so we keep track if the field has already been set
	private boolean filled;

	/**
	 * Creates a new {@link BasicTypeMapper} targeting an instance field on an instance.
	 *
	 * @param target The instance the field will be set on
	 * @param f      The field to set
	 * @throws CommandlineException If accessing the field for setter creation fails
	 */
	BasicTypeMapper(Object target, Field f) throws CommandlineException {
		f.setAccessible(true);

		try {
			setter = CommandlineParser.METHOD_LOOKUP.unreflectSetter(f).bindTo(target);
		} catch (IllegalAccessException e) {
			throw new CommandlineException("Insufficient access rights to unreflect setter", e);
		}
	}

	/**
	 * Sets the value of the field and marks this mapper as filled.
	 *
	 * @param value The value to set the field to
	 * @throws CommandlineException If something goes wrong while invoking the setter, for example
	 *                              if the field is
	 *                              of a different type than the supplied value
	 */
	protected void set(T value) throws CommandlineException {
		try {
			setter.invoke(value);
			filled = true;
		} catch (WrongMethodTypeException | ClassCastException e) {
			throw new CommandlineException("Tried to invoke setter with invalid value type", e);
		} catch (Throwable t) {
			throw new CommandlineException("Error occurred invoking setter", t);
		}
	}

	/**
	 * Checks wether this field has been filled, for a {@link BasicTypeMapper} this will be {@code
	 * true} after
	 * {@link BasicTypeMapper#set(Object)} has been called.
	 *
	 * @return True if the field has been set, false otherwise
	 */
	@Override
	public boolean filled() {
		return filled;
	}
}

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

import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.patchworkmc.commandline.CommandlineException;
import com.patchworkmc.commandline.CommandlineParseException;
import com.patchworkmc.commandline.CommandlineParser;

/**
 * Type mapper for converting a flag into an enum.
 *
 * @see BasicTypeMapper BasicTypeMapper for the base implementation
 */
public class EnumTypeMapper<E extends Enum<E>> extends BasicTypeMapper<Enum<E>> {
	private final Enum<E>[] enumValues;

	@SuppressWarnings("unchecked")
	public EnumTypeMapper(Class<E> enumClass, Object target, Field f) throws CommandlineException {
		super(target, f);

		if (!enumClass.isEnum()) {
			throw new CommandlineException("Tried to apply EnumTypeMapper to non enum type");
		}

		try {
			enumValues = (E[]) CommandlineParser.METHOD_LOOKUP.findStatic(enumClass, "values",
					MethodType.methodType(Array.newInstance(enumClass, 0).getClass())).invoke();
		} catch (NoSuchMethodException e) {
			throw new CommandlineException("BUG: Failed to find values method on enum  " + enumClass.getName());
		} catch (IllegalAccessException e) {
			throw new CommandlineException("Failed to access values method on enum " + enumClass.getName());
		} catch (Throwable t) {
			throw new CommandlineException("Exception while invoking values method on enum " + enumClass.getName(), t);
		}
	}

	/**
	 * Sets the underlying field to the value parsed as a value of {@code Enum<E>}.
	 *
	 * @param value The value to parse
	 * @throws CommandlineParseException If {@code value} is not a valid value for {@code Enum<E>}
	 * @throws CommandlineException      If an error occurs setting the field
	 */
	@Override
	public void apply(String value) throws CommandlineException {
		// First, try to match the name with case
		for (Enum<E> v : enumValues) {
			if (v.name().equals(value)) {
				set(v);
				return;
			}
		}

		// If matching with case didn't succeed, try matching without case
		for (Enum<E> v : enumValues) {
			if (v.name().equalsIgnoreCase(value)) {
				set(v);
				return;
			}
		}

		throw new CommandlineParseException(value + " must be one of "
				+ Stream.of(enumValues).map(Enum::name).collect(Collectors.joining(", ")));
	}

	/**
	 * Always true since we require an {@code String} to construct the value of {@code Enum<E>} from.
	 *
	 * @return Always {@code true}
	 */
	@Override
	public boolean acceptsValue() {
		return true;
	}
}

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

package com.patchworkmc.commandline.types;

import java.lang.reflect.Field;

import com.patchworkmc.commandline.CommandlineException;

/**
 * Type mapper for identity mapping String values.
 *
 * @see BasicTypeMapper BasicTypeMapper for the base implementation
 */
public class StringTypeMapper extends BasicTypeMapper<String> {
	public StringTypeMapper(Object target, Field f) throws CommandlineException {
		super(target, f);

		if (f.getType() != String.class) {
			throw new CommandlineException("Tried to apply mapper for String to field of type " + f.getType().getName());
		}
	}

	/**
	 * Sets the underlying field to the value specified.
	 *
	 * @param value The value to set the field to
	 * @throws CommandlineException If an error occurs setting the underlying field
	 */
	@Override
	public void apply(String value) throws CommandlineException {
		set(value);
	}

	/**
	 * Always returns {@code true} since we need a String to set the field to.
	 *
	 * @return Always {@code true}
	 */
	@Override
	public boolean acceptsValue() {
		return true;
	}
}

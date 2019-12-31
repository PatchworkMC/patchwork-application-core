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
import java.util.Arrays;
import java.util.List;

import com.patchworkmc.commandline.CommandlineException;

/**
 * Type mapper for converting <b>a flag with no value</b> into a boolean.
 *
 * @see BasicTypeMapper BasicTypeMapper for the base implementation
 */
public class BooleanTypeMapper extends BasicTypeMapper<Boolean> {
	private static final List<Class<?>> SUPPORTED_TYPES = Arrays.asList(Boolean.class, boolean.class, Boolean.TYPE);

	public BooleanTypeMapper(Object target, Field f) throws CommandlineException {
		super(target, f);

		if (!SUPPORTED_TYPES.contains(f.getType())) {
			throw new CommandlineException("Tried to apply mapper for type Boolean to field of type " + f.getType().getName());
		}
	}

	/**
	 * Sets the field targeted by this mapper to {@code true}.
	 * As specified by {@link BooleanTypeMapper#acceptsValue()} this mapper does not accept a value
	 * and thus expects the parameter to this method to be null!
	 *
	 * @param value Ignored, if asserts enabled asserted to be null
	 * @throws CommandlineException If an error occurs while setting the underlying field to {@code
	 *                              true}
	 */
	@Override
	public void apply(String value) throws CommandlineException {
		assert value == null;
		set(true);
	}

	/**
	 * Always returns {@code false} as calling {@link BooleanTypeMapper#apply(String)} expects the
	 * value to be
	 * {@code null}.
	 *
	 * @return Always {@code false}
	 */
	@Override
	public boolean acceptsValue() {
		return false;
	}
}

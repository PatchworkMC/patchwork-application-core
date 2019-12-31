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

package com.patchworkmc.json;

/**
 * Exception that may occur when converting data to a Json object.
 * Since this is just a wrapper exception, it always has a cause.
 */
public class JsonConverterException extends Exception {
	/**
	 * Creates a new {@link JsonConverterException}.
	 *
	 * @param msg The message of the exception
	 * @param t   The exception causing this exception
	 */
	public JsonConverterException(String msg, Throwable t) {
		super(msg, t);
	}
}

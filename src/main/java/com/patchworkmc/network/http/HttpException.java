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

package com.patchworkmc.network.http;

/**
 * Exception that may occur while dealing with Http connections.
 */
public class HttpException extends Exception {
	/**
	 * Creates a new {@link HttpException} with the specified message.
	 *
	 * @param msg The message of the exception.
	 */
	public HttpException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new {@link HttpException} which was caused by another
	 * exception with the specified message.
	 *
	 * @param msg   The message of the exception
	 * @param cause The exception which caused this exception
	 */
	public HttpException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

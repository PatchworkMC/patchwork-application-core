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

package com.patchworkmc.commandline;

/**
 * Base {@link Exception} for representing errors that can occur while working with the commandline.
 * Note that a {@link CommandlineParseException} is based on this class but will usually not be
 * exposed to the user.
 */
public class CommandlineException extends Exception {
	/**
	 * Creates a new {@link CommandlineException} with the specified message.
	 *
	 * @param message The message of the exception
	 */
	public CommandlineException(String message) {
		super(message);
	}

	/**
	 * Creates a new {@link CommandlineException} with the specified message
	 * and a cause.
	 *
	 * @param message The message of the exception
	 * @param cause   The exception which caused this exception
	 */
	public CommandlineException(String message, Throwable cause) {
		super(message, cause);
	}
}

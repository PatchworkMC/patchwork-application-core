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

/**
 * {@link Exception} representing parse errors for commandline values, for example if the user types
 * in a value which is not a number, but a number was expected. Note that this class is usually not
 * exposed to the user in terms of need of being caught.
 */
public class CommandlineParseException extends CommandlineException {
	/**
	 * Creates a new {@link CommandlineParseException} with the specified message.
	 * This message will be displayed to the user.
	 *
	 * @param message The message of the exception
	 */
	public CommandlineParseException(String message) {
		super(message);
	}

	/**
	 * Creates a new {@link CommandlineException} with a message and a cause.
	 * The message will be displayed to the user.
	 *
	 * @param message The message of the exception
	 * @param cause   The exception which caused this exception
	 */
	public CommandlineParseException(String message, Throwable cause) {
		super(message, cause);
	}
}

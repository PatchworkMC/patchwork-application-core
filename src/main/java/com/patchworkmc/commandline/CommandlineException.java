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

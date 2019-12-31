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

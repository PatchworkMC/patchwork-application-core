package com.patchworkmc.logging;

/**
 * Interface representing a simple logger backend.
 */
public interface LogWriter {
	/**
	 * Writes a message at a specified log level.
	 *
	 * @param level   The level to log at
	 * @param tag     The tag of the message
	 * @param message The message to log, may include newlines!
	 */
	void log(LogLevel level, String tag, String message);
}

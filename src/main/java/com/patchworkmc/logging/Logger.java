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

package com.patchworkmc.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a simple logger supporting multiple backends in form of {@link LogWriter}s.
 */
public class Logger {
	private final Map<LogWriter, LogLevel> writers;

	private final String tag;

	/**
	 * Creates a new logger with the specified tag.
	 *
	 * @param tag Tag of the logger
	 */
	public Logger(String tag) {
		this.tag = tag;
		writers = new HashMap<>();
	}

	private Logger(String tag, Map<LogWriter, LogLevel> writers) {
		this.tag = tag;
		this.writers = writers;
	}

	/**
	 * Sets a writer to a specific log level.
	 *
	 * @param writer The writer to set
	 * @param level  The log level the write should log at
	 */
	public void setWriter(LogWriter writer, LogLevel level) {
		this.writers.put(writer, level);
	}

	public void clearWriters() {
		this.writers.clear();
	}

	/**
	 * Logs a message with the specified log level to all writers having that level enabled.
	 *
	 * @param level  The level to log at
	 * @param format The format string
	 * @param args   The format arguments to apply to the string using
	 *               {@link String#format(String format, Object... args)}
	 */
	public void log(LogLevel level, String format, Object... args) {
		String formattedLog = String.format(format, args) + "\n";
		writers.forEach((writer, writerLevel) -> {
			if (writerLevel.includes(level)) {
				writer.log(level, tag, formattedLog);
			}
		});
	}

	/**
	 * Logs a message with the {@link LogLevel#TRACE} level to all writes having the TRACE level
	 * enabled.
	 *
	 * @param format The format string
	 * @param args   The format arguments to apply to the string using
	 *               {@link String#format(String format, Object... args)}
	 * @see Logger#log(LogLevel, String, Object...)
	 */
	public void trace(String format, Object... args) {
		log(LogLevel.TRACE, format, args);
	}

	/**
	 * Logs a message with the {@link LogLevel#DEBUG} level to all writes having the DEBUG level
	 * enabled.
	 *
	 * @param format The format string
	 * @param args   The format arguments to apply to the string using
	 *               {@link String#format(String format, Object... args)}
	 * @see Logger#log(LogLevel, String, Object...)
	 */
	public void debug(String format, Object... args) {
		log(LogLevel.DEBUG, format, args);
	}

	/**
	 * Logs a message with the {@link LogLevel#INFO} level to all writes having the INFO level
	 * enabled.
	 *
	 * @param format The format string
	 * @param args   The format arguments to apply to the string using
	 *               {@link String#format(String format, Object... args)}
	 * @see Logger#log(LogLevel, String, Object...)
	 */
	public void info(String format, Object... args) {
		log(LogLevel.INFO, format, args);
	}

	/**
	 * Logs a message with the {@link LogLevel#WARN} level to all writes having the WARN level
	 * enabled.
	 *
	 * @param format The format string
	 * @param args   The format arguments to apply to the string using
	 *               {@link String#format(String format, Object... args)}
	 * @see Logger#log(LogLevel, String, Object...)
	 */
	public void warn(String format, Object... args) {
		log(LogLevel.WARN, format, args);
	}

	/**
	 * Logs a message with the {@link LogLevel#ERROR} level to all writes having the ERROR level
	 * enabled.
	 *
	 * @param format The format string
	 * @param args   The format arguments to apply to the string using
	 *               {@link String#format(String format, Object... args)}
	 * @see Logger#log(LogLevel, String, Object...)
	 */
	public void error(String format, Object... args) {
		log(LogLevel.ERROR, format, args);
	}

	/**
	 * Logs a message with the {@link LogLevel#FATAL} level to all writes having the FATAL level
	 * enabled.
	 *
	 * @param format The format string
	 * @param args   The format arguments to apply to the string using
	 *               {@link String#format(String format, Object... args)}
	 * @see Logger#log(LogLevel, String, Object...)
	 */
	public void fatal(String format, Object... args) {
		log(LogLevel.FATAL, format, args);
	}

	/**
	 * Logs a {@link Throwable} and its stack trace pretty printed at a specific log level.
	 *
	 * @param level The level to log at
	 * @param cause The throwable to log
	 */
	public void thrown(LogLevel level, Throwable cause) {
		boolean first = true;
		StringBuilder messageBuffer = new StringBuilder();

		// This loop essentially emulates the default java stack trace printing
		// note that it is not 100% accurate
		while (cause != null) {
			if (first) {
				messageBuffer.append((cause instanceof Exception) ? "Exception" : "Throwable").append(" thrown in thread ").append(Thread.currentThread().getName());
			} else {
				messageBuffer.append("\nCaused by ").append((cause instanceof Exception) ? "Exception" : "Throwable");
			}

			messageBuffer.append(" ").append(cause.getClass().getName()).append(": ").append(cause.getLocalizedMessage());

			for (StackTraceElement element : cause.getStackTrace()) {
				messageBuffer.append("\n    at ")
						.append(element.getClassName())
						.append(".")
						.append(element.getMethodName())
						.append("(")
						.append(element.getFileName())
						.append(":")
						.append(element.getLineNumber())
						.append(")");
			}

			cause = cause.getCause();
			first = false;
		}

		String message = messageBuffer.toString();
		log(level, message);
	}

	/**
	 * Creates a new logger as a child of the current one, transferring over the tag and writers.
	 *
	 * @param subTag The sub tag, will be appended to the current tag with a {@code /} in front
	 * @return The child logger
	 */
	public Logger sub(String subTag) {
		return new Logger(tag + "/" + subTag, writers);
	}
}

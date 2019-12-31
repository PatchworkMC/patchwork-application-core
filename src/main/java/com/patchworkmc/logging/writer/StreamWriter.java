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

package com.patchworkmc.logging.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.fusesource.jansi.Ansi;

import com.patchworkmc.logging.LogLevel;
import com.patchworkmc.logging.LogWriter;

/**
 * Logger backend writing messages to {@link OutputStream}s.
 */
public class StreamWriter implements LogWriter {
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final boolean color;
	private final OutputStream out;
	private final OutputStream err;
	private OutputStream last;

	/**
	 * Constructs a new StreamWriter.
	 *
	 * @param color Determines if colors should be enabled
	 * @param out   The stream to treat as the standard output (for example {@link System#out})
	 * @param err   The stream to treat as the error output (for example {@link System#err})
	 */
	public StreamWriter(boolean color, OutputStream out, OutputStream err) {
		this.color = color;
		this.out = out;
		this.err = err;
	}

	@Override
	public synchronized void log(LogLevel level, String tag, String message) {
		List<byte[]> messages = new ArrayList<>();
		String prefix = prefix(level, tag);

		// Split the message at \n so that we don't have weird
		// breaks in the log output
		for (String part : message.split("\n")) {
			messages.add((prefix + part + "\n").getBytes());
		}

		if (!LogLevel.WARN.includes(level)) {
			try {
				for (byte[] msg : messages) {
					if (last == err && last != null) {
						err.flush();
					}

					out.write(msg);
					last = out;
				}
			} catch (IOException e) {
				// Simply terminate, logging failed, we can't really "log" the exception
				throw new RuntimeException(e);
			}
		} else {
			try {
				for (byte[] msg : messages) {
					if (last == out && last != null) {
						out.flush();
					}

					err.write(msg);
					last = err;
				}
			} catch (IOException e) {
				// Simply terminate, logging failed, we can't really "log" the exception
				throw new RuntimeException(e);
			}
		}
	}

	// Helper for generating a prefix for a specific log level
	private String prefix(LogLevel level, String tag) {
		String logPrefix;

		switch (level) {
		case TRACE:
			logPrefix = color ? Ansi.ansi().fgBrightMagenta().a("TRACE").toString() : "TRACE";
			break;

		case DEBUG:
			logPrefix = color ? Ansi.ansi().fgYellow().a("DEBUG").toString() : "DEBUG";
			break;

		case INFO:
			logPrefix = color ? Ansi.ansi().fgBrightBlue().a("INFO").toString() : "INFO";
			break;

		case WARN:
			logPrefix = color ? Ansi.ansi().fgBrightYellow().a("WARN").toString() : "WARN";
			break;

		case ERROR:
			logPrefix = color ? Ansi.ansi().fgBrightRed().a("ERROR").toString() : "ERROR";
			break;

		case FATAL:
			logPrefix = color ? Ansi.ansi().fgRed().a("FATAL").toString() : "FATAL";
			break;

		default:
			throw new AssertionError("UNREACHABLE");
		}

		if (color) {
			return Ansi.ansi()
					.reset()
					.fgBrightBlack()
					.a("[").reset()
					.a(logPrefix).reset()
					.fgBrightBlack().a("] ")
					.fgBrightYellow().a(DATE_TIME_FORMATTER.format(LocalDateTime.now()))
					.fgBrightBlack().a(Ansi.Attribute.UNDERLINE).a(" ").a(tag).reset()
					.fgBrightBlack().a(": ").reset()
					.toString();
		} else {
			return "[" + logPrefix + "] " + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " " + tag + ": ";
		}
	}
}

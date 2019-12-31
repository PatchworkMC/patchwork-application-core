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

package com.patchworkmc.logging;

/**
 * Represents a log level.
 */
public enum LogLevel {
	TRACE(5000),
	DEBUG(10000),
	INFO(20000),
	WARN(30000),
	ERROR(40000),
	FATAL(50000);

	private final int numericalLevel;

	LogLevel(int numericalLevel) {
		this.numericalLevel = numericalLevel;
	}

	/**
	 * Numerical representation of the log level for comparison with other levels.
	 *
	 * @return Numerical representation of the log level
	 */
	public int numerical() {
		return numericalLevel;
	}

	/**
	 * Determines wether this log level includes another one. For example, INFO includes WARN, but
	 * WARN does not include INFO.
	 *
	 * @param other The log level to check if its included
	 * @return {@code true} if this level includes (enables) the other level, {@code false}
	 * otherwise. Checking if a level includes will always be {@code true}.
	 */
	public boolean includes(LogLevel other) {
		return this.numericalLevel <= other.numericalLevel;
	}
}

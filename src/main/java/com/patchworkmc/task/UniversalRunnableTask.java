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

package com.patchworkmc.task;

import com.patchworkmc.logging.Logger;
import com.patchworkmc.function.ThrowingRunnable;

/**
 * {@link Task} for {@link Runnable}s and {@link ThrowingRunnable}s.
 */
public class UniversalRunnableTask extends Task {
	private final String name;
	private final ThrowingRunnable<Throwable> runnable;

	/**
	 * Creates a new {@link UniversalRunnableTask} with the specified name and runnable.
	 *
	 * @param name     The name of the task
	 * @param runnable The runnable to run as the task itself
	 */
	public UniversalRunnableTask(String name, ThrowingRunnable<Throwable> runnable) {
		this.name = name;
		this.runnable = runnable;
	}

	/**
	 * Creates a new {@link UniversalRunnableTask} with the specified name and runnable.
	 *
	 * @param name     The name of the task
	 * @param runnable The runnable to run as the task itself
	 */
	public UniversalRunnableTask(String name, Runnable runnable) {
		this.name = name;
		// We can't directly assign the runnable, but we can make a
		// method reference out of it
		this.runnable = runnable::run;
	}

	@Override
	protected boolean run(Logger logger) throws Throwable {
		logger.debug("Running universal runnable.");
		runnable.run();
		return true;
	}

	@Override
	public String name() {
		return name;
	}
}

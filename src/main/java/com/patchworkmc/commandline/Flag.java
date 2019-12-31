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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking a field as a flag in the type of an object passed to a {@link
 * CommandlineParser}.
 * <p>The field may be private since access will be acquired via reflection.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Flag {
	/**
	 * <p>Names of this flag.</p>
	 *
	 * <p>The following rules apply:
	 * <ul>
	 *     <li>If <code>name.length() &lt; 2</code>, the name on the commandline will be <code>"-"
	 *     + name</code></li> <li>Else the name will be <code>"--" + name</code></li>
	 * </ul>
	 *
	 *
	 * <p>Examples:
	 * <ul>
	 *     <li><code>"a"</code> will end up being <code>"-a"</code></li>
	 *     <li><code>"-"</code> will end up being <code>"--"</code></li>
	 *     <li><code>"hello"</code> will end up being <code>"--hello"</code></li>
	 * </ul>
	 *
	 *
	 * @return Names of this flag
	 */
	String[] names();

	/**
	 * A description of what this flag does. Newlines can be freely used, should even be to avoid
	 * lines getting too long. Too long means being wrapped by the terminal and thus it can't be
	 * really defined what the limit is.
	 *
	 * @return A description of what this flag does
	 */
	String description();

	/**
	 * The type mapper to use to map this value to. Defaults to a "null" type in which case the
	 * {@link CommandlineParser} will search for the correct one.
	 *
	 * @return The type mapper to use or {@link TypeMapper.NullTypeMapper} if the default one should
	 * be used
	 */
	Class<? extends TypeMapper> typeMapper() default TypeMapper.NullTypeMapper.class;
}

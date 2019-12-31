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

package com.patchworkmc.json;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for converting json input data to Java objects.
 */
public class JsonConverter {
	/**
	 * Global mapper used for conversion. Applying options to it affects
	 * all operations done with the {@link JsonConverter} class.
	 */
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/**
	 * Converts the data from an {@link InputStream} to a Java object.
	 *
	 * @param stream      The stream to read data from
	 * @param targetClass The class the target object should be of
	 * @param <T>         The type the target object should be of
	 * @return The data from the stream converted to the type of the target object
	 * @throws JsonConverterException If an error occurs while creating the object,
	 *                                such as IO errors or invalid json
	 */
	public static <T> T streamToObject(InputStream stream, Class<T> targetClass) throws JsonConverterException {
		try {
			return OBJECT_MAPPER.readValue(stream, targetClass);
		} catch (JsonParseException e) {
			throw new JsonConverterException("Got malformed json", e);
		} catch (JsonMappingException e) {
			throw new JsonConverterException("JsonMappingException while converting object", e);
		} catch (IOException e) {
			throw new JsonConverterException("IOException while reading from stream", e);
		}
	}

	private JsonConverter() {
	}
}

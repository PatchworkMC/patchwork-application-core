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

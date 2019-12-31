package com.patchworkmc.network.http.request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.patchworkmc.json.JsonConverter;
import com.patchworkmc.json.JsonConverterException;
import com.patchworkmc.network.http.HttpClient;
import com.patchworkmc.network.http.HttpException;
import com.patchworkmc.util.IThrowingSupplier;

/**
 * Represents a Http post request.
 */
public class HttpPostRequest extends HttpRequest<HttpPostRequest> {
	// Since we defer exceptions, store a function for retrieving
	// the actual body which may throw a HttpException
	private IThrowingSupplier<String, HttpException> body;

	/**
	 * Creates a new {@link HttpPostRequest}.
	 *
	 * @param client The http client this request will later be executed on
	 */
	public HttpPostRequest(HttpClient client) {
		super(client);
	}

	/**
	 * Sets the body of this request.
	 *
	 * @param body The new body
	 * @return this
	 */
	public HttpPostRequest body(String body) {
		this.body = () -> body;
		return this;
	}

	/**
	 * Sets the body of this request from an object converted to json.
	 * <b>Note that this does not set the content type header!</b>
	 *
	 * @param body The new body
	 * @return this
	 */
	public HttpPostRequest jsonBody(Object body) {
		this.body = () -> {
			try {
				// Convert the object to actual json
				return JsonConverter.OBJECT_MAPPER.writeValueAsString(body);
			} catch (JsonProcessingException e) {
				throw new HttpException("Failed to convert body to json");
			}
		};
		return this;
	}

	/**
	 * Executes the request and parses the response into Java object using {@link JsonConverter}.
	 *
	 * @param targetClass The class of the target object
	 * @param <T>         The type of the target object
	 * @return The target object created from the json response
	 * @throws HttpException          If an error occurs while connecting to the target
	 * @throws JsonConverterException If an error occurs while converting the response
	 *                                to the target object
	 */
	public <T> T executeAndParseJson(Class<T> targetClass) throws HttpException, JsonConverterException {
		HttpURLConnection urlConnection = client.openConnection(this);

		try {
			// Set the request method
			urlConnection.setRequestMethod("POST");

			// Retrieve the body if any
			String realBody = body != null ? body.get() : null;
			urlConnection.connect();

			// If we have a body, send it
			if (realBody != null) {
				urlConnection.setDoOutput(true);
				urlConnection.getOutputStream().write(realBody.getBytes(StandardCharsets.UTF_8));
				urlConnection.getOutputStream().flush();
			}

			if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new HttpException("Server returned HTTP status code " + urlConnection.getResponseCode());
			}

			// Convert the object on the fly
			return JsonConverter.streamToObject(urlConnection.getInputStream(), targetClass);
		} catch (IOException e) {
			throw new HttpException("IOException while connecting to " + urlConnection.getURL().toExternalForm(), e);
		} finally {
			// Make sure we disconnect
			urlConnection.disconnect();
		}
	}
}

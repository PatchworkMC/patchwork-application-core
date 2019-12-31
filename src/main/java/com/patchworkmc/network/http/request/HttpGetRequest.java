package com.patchworkmc.network.http.request;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.patchworkmc.json.JsonConverter;
import com.patchworkmc.json.JsonConverterException;
import com.patchworkmc.network.http.HttpClient;
import com.patchworkmc.network.http.HttpException;

/**
 * Represents a Http get request.
 */
public class HttpGetRequest extends HttpRequest<HttpGetRequest> {
	/**
	 * Creates a new {@link HttpGetRequest}.
	 *
	 * @param client The http client this request will later be executed on
	 */
	public HttpGetRequest(HttpClient client) {
		super(client);
	}

	/**
	 * Executes the request and retrieves the stream yielded.
	 *
	 * @return The stream this request yielded
	 * @throws HttpException If an error occurs while connecting to the target
	 */
	public InputStream executeAndGetStream() throws HttpException {
		HttpURLConnection urlConnection = client.openConnection(this);

		try {
			// Set the request method and open the connection
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

			if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new HttpException("Server returned HTTP status code " + urlConnection.getResponseCode());
			}

			return urlConnection.getInputStream();
		} catch (IOException e) {
			throw new HttpException("IOException while connecting to " + urlConnection.getURL().toExternalForm(), e);
		}
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
			// Set the request method and open the connection
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

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

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

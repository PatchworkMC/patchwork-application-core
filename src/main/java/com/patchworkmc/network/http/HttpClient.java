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

package com.patchworkmc.network.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.stream.Collectors;

import com.patchworkmc.network.http.request.HttpGetRequest;
import com.patchworkmc.network.http.request.HttpPostRequest;
import com.patchworkmc.network.http.request.HttpRequest;

/**
 * Simple Http client based on Java standard provided classes with cookie support.
 */
public class HttpClient {
	private final CookieManager cookieManager;

	public HttpClient() {
		this.cookieManager = new CookieManager();
	}

	/**
	 * Opens a connection using the specified http request. Note that you
	 * should not need to call this method directly, as the the requests
	 * do that on their own when you call an {@code execute} method on them.
	 *
	 * @param request The request to open the connection with
	 * @return The opened connection
	 * @throws HttpException If an error occurs while opening the connection or
	 *                       the request is invalid
	 */
	public HttpURLConnection openConnection(HttpRequest<?> request) throws HttpException {
		try {
			// Make sure the request is valid and then build
			// the target url
			request.validate();
			URI uri = request.buildUri();
			URL url = uri.toURL();

			// Open the connection
			URLConnection connection = url.openConnection();

			if (!(connection instanceof HttpURLConnection)) {
				// We are a Http client, not an URLConnection client...
				throw new HttpException("Opening URL " + url.toExternalForm() + " didn't yield a HTTP connection");
			} else {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;

				// Set the headers on the request
				request.applyHeaders(httpConnection);
				httpConnection.setRequestProperty("Cookie",
						cookieManager
								.getCookieStore()
								.get(uri)
								.stream()
								.map(HttpCookie::toString)
								.collect(Collectors.joining(";")));
				return httpConnection;
			}
		} catch (IOException e) {
			throw new HttpException("IOException while opening url connection", e);
		}
	}

	/**
	 * Creates a new get request (does NOT execute it).
	 *
	 * @return The created get request
	 */
	public HttpGetRequest get() {
		return new HttpGetRequest(this);
	}

	/**
	 * Creates a new get request to the specified url (does NOT execute it).
	 *
	 * @param url The url to send the request to later
	 * @return The created get request
	 */
	public HttpGetRequest get(String url) {
		return new HttpGetRequest(this).url(url);
	}

	/**
	 * Creates a new post request (does NOT execute it).
	 *
	 * @return The created post request
	 */
	public HttpPostRequest post() {
		return new HttpPostRequest(this);
	}

	/**
	 * Creates a new post request to the specified url (does NOT execute it).
	 *
	 * @param url The url to send the request to later
	 * @return The created post request
	 */
	public HttpPostRequest post(String url) {
		return new HttpPostRequest(this).url(url);
	}

	/**
	 * Creates an URI from an URL, even if it contains unwise characters.
	 *
	 * @param url The URL to create the URI from
	 * @return The URL converted to an URI
	 * @throws HttpException If an error occurs while converting
	 */
	public static URI makeURI(URL url) throws HttpException {
		try {
			// This weird way of creating the URI prevents problems
			// with unwise chars, see
			// https://stackoverflow.com/questions/13530019/how-to-convert-url-touri-when-there-are-unwise-characters
			return new URI(
					url.getProtocol(),
					null,
					url.getHost(),
					url.getPort(),
					(url.getPath() == null) ? null : URLDecoder.decode(url.getPath(), "UTF-8"),
					(url.getQuery() == null) ? null : URLDecoder.decode(url.getQuery(), "UTF-8"),
					null);
		} catch (URISyntaxException e) {
			throw new HttpException("Failed to create URI", e);
		} catch (UnsupportedEncodingException e) {
			// Oh yeah, no UTF-8 for sure, would you like to try UTF-9?
			throw new HttpException("UTF-8 not supported", e);
		}
	}
}

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

package com.patchworkmc.network.http.request;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.patchworkmc.network.http.HttpClient;
import com.patchworkmc.network.http.HttpException;

/**
 * Base class for all Http request.
 *
 * @param <S> The real request type
 */
public abstract class HttpRequest<S extends HttpRequest<S>> {
	// The client this request will be executed on
	protected final HttpClient client;

	// Since we defer the throwing of exceptions,
	// we need to store it first
	protected Exception exception;

	// Metadata
	protected String protocol;
	protected String host;
	protected int port;
	protected String path;
	protected Map<String, String> query; // Unencoded query as key=value
	protected Map<String, String> headers;

	/**
	 * Creates a new {@link HttpRequest}.
	 *
	 * @param client The client this request will be executed on later
	 */
	HttpRequest(HttpClient client) {
		this.client = client;
		this.port = -2;
		this.query = new HashMap<>();
		this.headers = new HashMap<>();
	}

	/**
	 * Sets the target url.
	 *
	 * @param url The new target url
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public S url(String url) {
		try {
			return url(new URL(url));
		} catch (MalformedURLException e) {
			// Clear out fields in case the set failed
			protocol = null;
			host = null;
			port = -1;
			query.clear();

			exception = e;
		}

		return (S) this;
	}

	/**
	 * Sets the target url.
	 *
	 * @param url The new target url
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public S url(URL url) {
		// We need the URI later
		URI uri;

		try {
			uri = HttpClient.makeURI(url);
		} catch (HttpException e) {
			exception = e;
			return (S) this;
		}

		// Clear the exception since it would be invalid now
		exception = null;

		// Apply local fields
		protocol = url.getProtocol();
		host = url.getHost();
		port = url.getPort();
		path = url.getPath();

		// If the request has no port set, it will be -1 now,
		// so try to retrieve the default port
		if (port == -1) {
			port = url.getDefaultPort();
		}

		// We want the raw query so we can easily convert it back, since
		// apparently raw means the encoded one
		String rawQuery = uri.getRawQuery();

		if (rawQuery != null) {
			// If we have a query, map it
			try {
				// Split all the key-value pairs
				for (String queryPart : rawQuery.split("&")) {
					if (queryPart.contains("=")) {
						// We have a key with a value
						String[] split = queryPart.split("=");

						// The only valid form is key=value and key nor value
						// can contain '=' since they are encoded, so something is wrong
						if (split.length != 2) {
							exception = new URISyntaxException(
									uri.toASCIIString(), "Query part " + queryPart + " contains to many =");
						} else {
							// Register the query, for that, decode the elements first
							query.put(URLDecoder.decode(split[0], "UTF-8"), URLDecoder.decode(split[1], "UTF-8"));
						}
					} else {
						// We have a key without a value, decode the key and register it
						query.put(URLDecoder.decode(queryPart, "UTF-8"), null);
					}
				}
			} catch (UnsupportedEncodingException e) {
				// The system does not support UTF-8?
				// We better don't question this...
				exception = new HttpException("UTF-8 not supported", e);
				return (S) this;
			}
		}

		return (S) this;
	}

	/**
	 * Sets the target protocol.
	 *
	 * @param protocol The new target protocol
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public S protocol(String protocol) {
		this.protocol = protocol;
		return (S) this;
	}

	/**
	 * Sets the target host.
	 *
	 * @param host The new target host
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public S host(String host) {
		this.host = host;
		return (S) this;
	}

	/**
	 * Sets the target port.
	 *
	 * @param port The new target port
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public S port(int port) {
		this.port = port;
		return (S) this;
	}

	/**
	 * Sets the target path.
	 *
	 * @param path The new target path
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public S path(String path) {
		this.path = path;
		return (S) this;
	}

	/**
	 * Adds a key to the query without a value.
	 *
	 * @param key The key to add
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public S query(String key) {
		this.query.put(key, null);
		return (S) this;
	}

	/**
	 * Adds a key value pair to the query.
	 *
	 * @param key   The key of the pair to add
	 * @param value The value of the pair to add
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public S query(String key, Object value) {
		this.query.put(key, value.toString());
		return (S) this;
	}

	/**
	 * Replaces the query.
	 *
	 * @param query The new query
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public S query(Map<String, String> query) {
		this.query = query;
		return (S) this;
	}

	/**
	 * Adds a header to the request.
	 *
	 * @param key   The header name
	 * @param value The value of the header
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public S header(String key, String value) {
		this.headers.put(key, value);
		return (S) this;
	}

	/**
	 * Replaces the headers of the request.
	 *
	 * @param headers The new headers.
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public S headers(Map<String, String> headers) {
		this.headers = headers;
		return (S) this;
	}

	/**
	 * Validates the request. If the request is invalid, a {@link HttpException}
	 * with a message explaining why it is invalid, is thrown.
	 *
	 * @throws HttpException If the request is invalid
	 */
	public void validate() throws HttpException {
		// Try to figure out what port to use
		if (port == -2 && protocol != null) {
			if (protocol.equals("http")) {
				port = 80;
			} else if (protocol.equals("https")) {
				port = 443;
			} else {
				throw new HttpException("Failed to determine default port for protocol " + protocol);
			}
		}

		if (exception != null) {
			if (!(exception instanceof HttpException)) {
				throw new HttpException("Cached internal exception", exception);
			} else {
				throw (HttpException) exception;
			}
		} else if (client == null) {
			throw new HttpException("httpClient is null", new NullPointerException());
		} else if (protocol == null) {
			throw new HttpException("protocol is null", new NullPointerException());
		} else if (host == null) {
			throw new HttpException("host is null", new NullPointerException());
		} else if (path == null) {
			throw new HttpException("path is null", new NullPointerException());
		} else if (port < 1 || port > 65535) {
			throw new HttpException("port " + port + " is out of range");
		}
	}

	/**
	 * Builds the query part of the url.
	 *
	 * @return The query part without the '?'
	 * @throws HttpException If an error occurs building the query
	 */
	private String buildQuery() throws HttpException {
		StringBuilder builder = new StringBuilder();

		try {
			for (Map.Entry<String, String> entry : query.entrySet()) {
				builder
						.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
						.append("=")
						.append(URLEncoder.encode(entry.getValue(), "UTF-8"))
						.append("&");
			}
		} catch (UnsupportedEncodingException e) {
			// I'm not even going to question this...
			throw new HttpException("UTF-8 not supported", e);
		}

		if (builder.length() < 1) {
			return "";
		}

		// cut away last &
		return builder.substring(0, builder.length() - 1);
	}

	/**
	 * Builds the url this request points to.
	 *
	 * @return The url this request points to
	 * @throws HttpException If an error occurs building the URL
	 */
	public URI buildUri() throws HttpException {
		// Make sure neither host nor path end with a '/'
		if (host.endsWith("/")) {
			host = host.substring(0, host.length() - 1);
		}

		try {
			return new URI(protocol, null, host, port, path, buildQuery(), null);
		} catch (URISyntaxException e) {
			throw new HttpException("Failed to build URI to build URL");
		}
	}

	/**
	 * Sets the headers on the specified connection.
	 *
	 * @param urlConnection The connection to set the headers on
	 */
	public void applyHeaders(HttpURLConnection urlConnection) {
		headers.forEach(urlConnection::setRequestProperty);
	}
}

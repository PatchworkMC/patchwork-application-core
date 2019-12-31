package com.patchworkmc.network.http;

/**
 * Exception that may occur while dealing with Http connections.
 */
public class HttpException extends Exception {
	/**
	 * Creates a new {@link HttpException} with the specified message.
	 *
	 * @param msg The message of the exception.
	 */
	public HttpException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new {@link HttpException} which was caused by another
	 * exception with the specified message.
	 *
	 * @param msg   The message of the exception
	 * @param cause The exception which caused this exception
	 */
	public HttpException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

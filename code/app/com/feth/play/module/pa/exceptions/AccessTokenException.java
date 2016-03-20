package com.feth.play.module.pa.exceptions;

public class AccessTokenException extends AuthException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AccessTokenException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AccessTokenException(final String message) {
		super(message);
	}

	public AccessTokenException() {
		super();
	}

}

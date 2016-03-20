package com.feth.play.module.pa.exceptions;

public class AuthException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuthException(final String message) {
		super(message);
	}

	public AuthException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public AuthException() {
		super();
	}
}

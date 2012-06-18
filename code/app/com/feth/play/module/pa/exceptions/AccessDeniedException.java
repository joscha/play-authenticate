package com.feth.play.module.pa.exceptions;

public class AccessDeniedException extends AuthException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AccessDeniedException(final String message) {
		super(message);
	}

	public AccessDeniedException() {
		super();
	}

}

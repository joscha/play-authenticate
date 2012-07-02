package com.feth.play.module.pa.providers.openid.exceptions;

import com.feth.play.module.pa.exceptions.AuthException;

public class NoOpenIdAuthException extends AuthException {

	public NoOpenIdAuthException(final String string) {
		super(string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}

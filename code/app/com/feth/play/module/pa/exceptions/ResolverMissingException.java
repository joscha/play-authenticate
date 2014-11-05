package com.feth.play.module.pa.exceptions;

public class ResolverMissingException extends AuthException {

    private static final long serialVersionUID = 1L;

    public ResolverMissingException(final String message) {
        super(message);
    }
}

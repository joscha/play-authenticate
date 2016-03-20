package com.feth.play.module.pa.controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import play.mvc.Result;

public class AuthenticateDI extends AuthenticateBase {

	private PlayAuthenticate auth;

	public AuthenticateDI(PlayAuthenticate auth) {
		this.auth = auth;
	}

	public Result authenticate(final String provider) {
		noCache(response());
		final String payload = request().getQueryString(PAYLOAD_KEY);
		return this.auth.handleAuthentication(provider, ctx(), payload);
	}

	public Result logout() {
		noCache(response());
		return this.auth.logout(session());
	}
}

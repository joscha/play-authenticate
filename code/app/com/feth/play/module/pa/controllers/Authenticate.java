package com.feth.play.module.pa.controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import play.mvc.Result;

import javax.inject.Inject;

public class Authenticate extends AuthenticateBase {

	private PlayAuthenticate auth;

	@Inject
	public Authenticate(PlayAuthenticate auth) {
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

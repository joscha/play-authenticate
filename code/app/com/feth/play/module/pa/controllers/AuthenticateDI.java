package com.feth.play.module.pa.controllers;

import play.mvc.Controller;
import play.mvc.Result;

import com.feth.play.module.pa.PlayAuthenticate;

public class AuthenticateDI extends AuthenticateBase {

	public Result authenticate(final String provider) {
		noCache(response());
		final String payload = request().getQueryString(PAYLOAD_KEY);
		return PlayAuthenticate.handleAuthentication(provider, ctx(), payload);
	}

	public Result logout() {
		noCache(response());
		return PlayAuthenticate.logout(session());
	}
}

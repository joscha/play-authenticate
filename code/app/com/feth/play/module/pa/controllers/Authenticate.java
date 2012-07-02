package com.feth.play.module.pa.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.Request;

import com.feth.play.module.pa.PlayAuthenticate;

public class Authenticate extends Controller {

	
	private static final String PAYLOAD_KEY = "p";

	public static Result authenticate(final String provider) {
		final String payload = getQueryString(request(), PAYLOAD_KEY);
		return PlayAuthenticate.handleAuthentication(provider, ctx(), payload);
	}
	
	public static Result logout() {
		return PlayAuthenticate.logout(session());
	}

	// TODO remove on Play 2.1
	public static String getQueryString(final Request r, final Object key) {
		final String[] m = r.queryString().get(key);
		if(m != null && m.length > 0) {
			return m[0];
		}
		return null;
	}
}

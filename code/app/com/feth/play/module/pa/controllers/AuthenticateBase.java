package com.feth.play.module.pa.controllers;

import play.mvc.Controller;
import play.mvc.Http.Response;

import com.feth.play.module.pa.PlayAuthenticate;

public class AuthenticateBase extends Controller {

	protected static final String PAYLOAD_KEY = "p";

	public static void noCache(final Response response) {
		// http://stackoverflow.com/questions/49547/making-sure-a-web-page-is-not-cached-across-all-browsers
		response.setHeader(Response.CACHE_CONTROL, "no-cache, no-store, must-revalidate");  // HTTP 1.1
		response.setHeader(Response.PRAGMA, "no-cache");  // HTTP 1.0.
		response.setHeader(Response.EXPIRES, "0");  // Proxies.
	}
}

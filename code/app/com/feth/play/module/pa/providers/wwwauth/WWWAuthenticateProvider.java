/*
 * Copyright © 2014 Florian Hars, nMIT Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feth.play.module.pa.providers.wwwauth;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import play.inject.ApplicationLifecycle;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.twirl.api.Content;

/** A base class for browser based authentication using the WWW-Authenticate header.
 *
 * This does not fully implement the usual mechanism where a whole
 * site or directory is protected by one of these authentication
 * mechanisms. The intended use case is that it protects just a single
 * URL so that it can be used as one of play-authenticate's mechanisms.
 *
 * Unlike other mechanisms, it returns a formatted page on authentication
 * failure, which could for example be a login form for one or more of
 * the other mechanisms supported.
 */
public abstract class WWWAuthenticateProvider extends AuthProvider {

	public WWWAuthenticateProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle) {
		super(auth, lifecycle);
	}

	/** The name of the authentication scheme
	 *
	 * @return The name of the authentication scheme, like Basic or Negotiate
	 */
	protected abstract String authScheme();

	/** The challenge to provide to an unauthenticated client.
	 *
	 * @param context The current request context
	 * @return The challenge string to return (without the scheme name), or null
	 */
	protected abstract String challenge(Context context);

	/** Try to authenticate the incoming Request.
	 *
	 * @param response The response to the challenge (without the scheme name)
	 * @return An AuthUser or null if authentication failed
	 * @throws AuthException
	 */
	protected abstract AuthUser authenticateResponse(String response) throws AuthException;

	/** The 401 page to return to the browser if authentication failed.
	 *
	 * This could for example be a login form that submits to another
	 * authentication method.
	 *
	 * @param context The current request context
	 * @return The formatted unauthorized page
	 */
	protected Content unauthorized(Context context) {
		return new Content() {

			@Override
			public String body() {
				return "Go away, you don't exit.";
			}

			@Override
			public String contentType() {
				return "text/plain";
			}};
	}

	private Result deny(Context context) {
		String authChallenge = challenge(context);
		if (authChallenge == null) {
			authChallenge = authScheme();
		} else {
			authChallenge = authScheme()+" "+authChallenge;
		}
		context.response().setHeader("WWW-Authenticate", authChallenge);

		return Controller.unauthorized(unauthorized(context));
	}

	@Override
	public Object authenticate(Context context, Object payload)	throws AuthException {
		String auth = context.request().getHeader("Authorization");

		if (auth == null) {
			return deny(context);
		}
		int ix = auth.indexOf(32);
		if (ix == -1 || !authScheme().equalsIgnoreCase(auth.substring(0,ix))) {
			return deny(context);
		}
		AuthUser user = authenticateResponse(auth.substring(ix+1));
		if (user == null) {
			return deny(context);
		} else {
			return user;
		}
	}

	@Override
	public boolean isExternal() {
		return false;
	}

}

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
package com.feth.play.module.pa.providers.wwwauth.basic;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.wwwauth.WWWAuthenticateProvider;
import com.feth.play.module.pa.user.AuthUser;
import play.inject.ApplicationLifecycle;
import play.mvc.Http.Context;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

/** A provider for RFC 2617 Basic Authentication.
 *
 * This is the simplest possible HTTP authentication mechanism.
 * Owing to its dubious security properies, this mechanism should
 * probably not be used in production. This code mostly serves as
 * an example of how to implement subclasses of [WWWAuthenticateProvider].
 */
public abstract class BasicAuthProvider extends WWWAuthenticateProvider {

	public BasicAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle) {
		super(auth, lifecycle);
	}

	/** Check the provided credentials.
	 *
	 * @param username The user name supplied by the browser
	 * @param password The password supplied by the browser
	 * @return An AuthUser instance if the credentials are valid, null otherwise
	 */
	protected abstract AuthUser authenticateUser(String username, String password);

	@Override
	protected List<String> neededSettingKeys() {
		return Collections.singletonList(SettingKeys.REALM);
	}

	/** Basic auth has a single configuration parameter, the realm.
	 */
	public static abstract class SettingKeys {
		public static final String REALM = "realm";
	}

	@Override
	protected String authScheme() {
		return "Basic";
	}

	@Override
	protected String challenge(Context context) {
		String realm = getConfiguration().getString(SettingKeys.REALM);
		// TODO: Check that this is actually the correct encoding
		return String.format("realm=\"%s\"", Base64.getEncoder().encode(realm.getBytes()));
	}

	@Override
	protected AuthUser authenticateResponse(String response) {
		String decoded = new String(Base64.getDecoder().decode(response));
		String[] parts = decoded.split(":", 2);
		if (parts.length == 2) {
			return authenticateUser(parts[0], parts[1]);
		} else {
			return null;
		}
	}
}

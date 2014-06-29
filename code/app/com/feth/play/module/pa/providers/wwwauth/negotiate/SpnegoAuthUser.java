/*
 * Copyright © 2014 Florian Hars, nMIT Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feth.play.module.pa.providers.wwwauth.negotiate;

import java.util.Date;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;

import com.feth.play.module.pa.user.AuthUser;

public class SpnegoAuthUser extends AuthUser {

	private static final long serialVersionUID = -4019138063299221477L;

	private String principal;
	private long expiration;

	public SpnegoAuthUser(GSSContext context) throws GSSException {
		this.principal = context.getSrcName().toString();
		this.expiration = new Date().getTime() + 1000L * context.getLifetime();
	}

	@Override
	public String getId() {
		return principal;
	}

	@Override
	public String getProvider() {
		return SpnegoAuthProvider.PROVIDER_KEY;
	}

	@Override
	public long expires() {
		return expiration;
	}

}

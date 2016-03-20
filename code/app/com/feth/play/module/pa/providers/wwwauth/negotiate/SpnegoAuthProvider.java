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

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.wwwauth.WWWAuthenticateProvider;
import com.feth.play.module.pa.user.AuthUser;
import org.ietf.jgss.*;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.mvc.Http.Context;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Base64;

/** Authentication against a Windows Active Directory domain.
 *
 * This provider implements the minimal functionality you need if you just
 * want to ensure the the request comes from a user that is authenticated in
 * the domain.
 *
 * See the README.md in this directory for more detailed usage instructions.
 */
@Singleton
public class SpnegoAuthProvider extends WWWAuthenticateProvider {

	@Inject
	public SpnegoAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle) {
		super(auth, lifecycle);
		String realm = getConfiguration().getString(SettingKeys.REALM);
		String kdc = getConfiguration().getString(SettingKeys.KDC);
		if (realm != null && kdc != null) {
			System.setProperty("java.security.krb5.realm", realm);
			System.setProperty("java.security.krb5.kdc", kdc);
		} else if (realm != null || kdc != null) {
			Logger.error("Both realm and kdc must be given, falling back to krb5.conf");
		}
	}

	public final static String PROVIDER_KEY = "spnego";
	private static Oid SPNEGO_MECH_OID;
	static {
		try {
			SPNEGO_MECH_OID = new Oid("1.3.6.1.5.5.2");
		} catch (GSSException e) {
			Logger.error("SPNEGO Oid is undefined");
		}
	}

	/** The windows domain and AD controller are the Kerberos realm and KDC.
	 */
	public static abstract class SettingKeys {
		public static final String REALM = "realm";
		public static final String KDC = "kdc";
	}

	@Override
	protected String authScheme() {
		return "Negotiate";
	}

	@Override
	protected String challenge(Context context) {
		return null;
	}

	/** Instantiate an AuthUser object for a successfully authenticated request.
	 *
	 * Override this in a subclass if your AuthUser needs more information than
	 * just the principal name.
	 *
	 * @param gssContext
	 * @return
	 */
	protected AuthUser makeAuthUser(GSSContext gssContext) {
		try {
			return new SpnegoAuthUser(gssContext);
		} catch (GSSException e) {
			Logger.warn("Error creating SpnegoAuthUser", e);
			return null;
		}
	}

	@Override
	protected AuthUser authenticateResponse(String response) throws AuthException {
		if (response.startsWith("TlRMTVNTU")) {
			Logger.warn("Discarding deprecated NTLMSSP authentication attempt");
			return null;
		}
		byte[] token = Base64.getDecoder().decode(response);

		try {
			GSSManager gssManager = GSSManager.getInstance();
			GSSCredential gssCred = gssManager.createCredential(null, GSSCredential.DEFAULT_LIFETIME, SPNEGO_MECH_OID, GSSCredential.ACCEPT_ONLY);
			GSSContext gssContext = gssManager.createContext(gssCred);
			byte[] tokenForPeer = gssContext.acceptSecContext(token, 0, token.length);
			if (!gssContext.isEstablished()) {
				throw new AuthException("Couldn't establish GSS context");
			}
			if (tokenForPeer != null) {
				Logger.warn("Ignoring token for peer");
			}
			Logger.debug("Authenticated "+gssContext.getSrcName()+" with "+gssContext.getTargName());
			return makeAuthUser(gssContext);
		} catch (GSSException e) {
			throw new AuthException("SPNEGO authentication failed: " + e);
		}
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}
}

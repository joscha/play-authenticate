package com.feth.play.module.pa.providers.openid;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import play.Application;
import play.Configuration;
import play.Logger;
import play.libs.F.Promise;
import play.libs.OpenID;
import play.libs.OpenID.UserInfo;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.ext.ExternalAuthProvider;
import com.feth.play.module.pa.providers.openid.exceptions.NoOpenIdAuthException;
import com.feth.play.module.pa.providers.openid.exceptions.OpenIdConnectException;

public class OpenIdAuthProvider extends ExternalAuthProvider {

	static abstract class SettingKeys {
		public final static String PROVIDER_KEY = "openid";
		public static final String ATTRIBUTES = "attributes";
		public static final String ATTRIBUTES_REQUIRED = "required";
		public static final String ATTRIBUTES_OPTIONAL = "optional";
	}

	public OpenIdAuthProvider(final Application app) {
		super(app);
	}

	@Override
	public String getKey() {
		return SettingKeys.PROVIDER_KEY;
	}

	@Override
	public Object authenticate(final Context context, final Object payload)
			throws AuthException {

		final Request request = context.request();

		if (Logger.isDebugEnabled()) {
			Logger.debug("Returned with URL: '" + request.uri() + "'");
		}

		final boolean hasOpenID = payload != null
				&& !payload.toString().trim().isEmpty();
		boolean hasInfo = false;
		UserInfo u = null;
		try {
			final Promise<UserInfo> pu = OpenID.verifiedId();
			u = pu.get();
			hasInfo = true;
		} catch (final Throwable t) {
			if (t instanceof play.api.libs.openid.Errors$BAD_RESPONSE$) {
				if (!hasOpenID) {
					throw new NoOpenIdAuthException(
							"OpenID endpoint is required");
				} else {
					// ignore, its the start of the OpenID dance
				}
			} else if (t instanceof play.api.libs.openid.Errors$BAD_RESPONSE$) {
				throw new AuthException("Bad response from OpenID provider");
			} else {
				throw new AuthException(t.getMessage());
			}
		}
		if (hasInfo) {

			// TODO: Switch to passing the UserInfo only, when the fix for:
			// https://play.lighthouseapp.com/projects/82401-play-20/tickets/578-202-java-openid-userinfo-id-always-null
			// has been incorporated.
			return new OpenIdAuthUser(Authenticate.getQueryString(request,
					"openid.claimed_id"), u);

		} else if (hasOpenID) {
			final Map<String, String> required = getAttributes(SettingKeys.ATTRIBUTES_REQUIRED);
			final Map<String, String> optional = getAttributes(SettingKeys.ATTRIBUTES_OPTIONAL);

			try {
				final Promise<String> pr = OpenID.redirectURL(
						payload.toString(), getRedirectUrl(context.request()),
						required, optional);

				return pr.get();
			} catch (final Throwable t) {
				if (t instanceof java.net.ConnectException) {
					throw new OpenIdConnectException(t.getMessage());
				} else {
					throw new AuthException(t.getMessage());
				}
			}
		} else {
			// this must never happen
			throw new AuthException();
		}
	}

	private Map<String, String> getAttributes(final String subKey) {
		final Configuration attributes = getConfiguration().getConfig(
				SettingKeys.ATTRIBUTES + "." + subKey);
		if (attributes != null) {
			final Set<String> keys = attributes.keys();
			final Map<String, String> ret = new HashMap<String, String>(
					keys.size());
			for (final String key : keys) {
				ret.put(key, attributes.getString(key));
			}
			return ret;
		}

		return null;
	}

}

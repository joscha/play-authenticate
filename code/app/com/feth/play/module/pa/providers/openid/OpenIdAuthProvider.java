package com.feth.play.module.pa.providers.openid;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import play.Application;
import play.Configuration;
import play.Logger;
import play.api.libs.openid.OpenIDError;
import play.libs.F.Promise;
import play.libs.openid.OpenID;
import play.libs.openid.UserInfo;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.ext.ExternalAuthProvider;
import com.feth.play.module.pa.providers.openid.exceptions.NoOpenIdAuthException;
import com.feth.play.module.pa.providers.openid.exceptions.OpenIdConnectException;
import com.google.inject.Inject;

public class OpenIdAuthProvider extends ExternalAuthProvider {

	static abstract class SettingKeys {
		public final static String PROVIDER_KEY = "openid";
		public static final String ATTRIBUTES = "attributes";
		public static final String ATTRIBUTES_REQUIRED = "required";
		public static final String ATTRIBUTES_OPTIONAL = "optional";
	}

	@Inject
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

		if (!hasOpenID) {
            try {
                final Promise<UserInfo> pu = OpenID.verifiedId();
                return new OpenIdAuthUser(pu.get(getTimeout()));
            } catch (final Throwable t) {
                if (t instanceof OpenIDError) {
                    if (!hasOpenID) {
                        throw new NoOpenIdAuthException(
                                "OpenID endpoint is required");
                    } else {
                        if(((OpenIDError) t).message() != null) {
                            throw new AuthException(((OpenIDError) t).message());
                        } else {
                            throw new AuthException("Bad response from OpenID provider");
                        }
                    }
                } else {
                    throw new AuthException(t.getMessage());
                }
            }
		} else {
			final Map<String, String> required = getAttributes(SettingKeys.ATTRIBUTES_REQUIRED);
			final Map<String, String> optional = getAttributes(SettingKeys.ATTRIBUTES_OPTIONAL);

			try {
				final Promise<String> pr = OpenID.redirectURL(
						payload.toString(), getRedirectUrl(context.request()),
						required, optional);

				return pr.get(getTimeout());
			} catch (final Throwable t) {
				if (t instanceof java.net.ConnectException) {
					throw new OpenIdConnectException(t.getMessage());
				} else {
					throw new AuthException(t.getMessage());
				}
			}
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

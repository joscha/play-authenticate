package com.feth.play.module.pa.providers.openid;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.ext.ExternalAuthProvider;
import com.feth.play.module.pa.providers.openid.exceptions.NoOpenIdAuthException;
import com.feth.play.module.pa.providers.openid.exceptions.OpenIdConnectException;
import play.Configuration;
import play.Logger;
import play.api.libs.openid.OpenIDError;
import play.inject.ApplicationLifecycle;
import play.libs.openid.OpenIdClient;
import play.libs.openid.UserInfo;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Singleton
public class OpenIdAuthProvider extends ExternalAuthProvider {

	static abstract class SettingKeys {
		public final static String PROVIDER_KEY = "openid";
		public static final String ATTRIBUTES = "attributes";
		public static final String ATTRIBUTES_REQUIRED = "required";
		public static final String ATTRIBUTES_OPTIONAL = "optional";
	}

	private final OpenIdClient openIdClient;

	@Inject
	public OpenIdAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final OpenIdClient openIdClient) {
		super(auth, lifecycle);
		this.openIdClient = openIdClient;
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
                final Future<UserInfo> pu = openIdClient.verifiedId().toCompletableFuture();
                return new OpenIdAuthUser(pu.get(getTimeout(), MILLISECONDS));
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
				final Future<String> pr = openIdClient.redirectURL(
						payload.toString(), getRedirectUrl(context.request()),
						required, optional).toCompletableFuture();

				return pr.get(getTimeout(), MILLISECONDS);
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

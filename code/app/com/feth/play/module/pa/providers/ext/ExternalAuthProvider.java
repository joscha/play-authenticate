package com.feth.play.module.pa.providers.ext;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.exceptions.ResolverMissingException;
import com.feth.play.module.pa.providers.AuthProvider;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import play.inject.ApplicationLifecycle;
import play.mvc.Call;
import play.mvc.Http.Request;

import java.util.Collections;
import java.util.List;

public abstract class ExternalAuthProvider extends AuthProvider {

	private static abstract class SettingKeys {
		@Deprecated
		public static final String SECURE_REDIRECT_URI = "secureRedirectUri";
		public static final String REDIRECT_URI_HOST = "redirectUri.host";
		public static final String REDIRECT_URI_SECURE = "redirectUri.secure";
        public static final String TIMEOUT = "timeout";
	}

	protected static String generateURI(final String location,
			final List<? extends NameValuePair> params) {
		final HttpGet m = new HttpGet(location + "?"
				+ URLEncodedUtils.format(params, "UTF-8"));
		return m.getURI().toString();
	}

	@Override
	protected List<String> neededSettingKeys() {
		return Collections.singletonList(SettingKeys.TIMEOUT);
	}

    protected long getTimeout() {
        return getConfiguration().getLong(SettingKeys.TIMEOUT);
    }

	private boolean useSecureRedirectUri() {
		Boolean secure = getConfiguration().getBoolean(
				SettingKeys.REDIRECT_URI_SECURE);
		if (secure == null) {
            // only for backwards compatibility
			secure = getConfiguration().getBoolean(
					SettingKeys.SECURE_REDIRECT_URI);
		}
        return secure != null ? secure : false;
	}

	protected String getRedirectUrl(final Request request,
			final List<? extends NameValuePair> params) throws ResolverMissingException {
		return generateURI(getRedirectUrl(request), params);
	}

	protected String getRedirectUrl(final Request request) throws ResolverMissingException {
		final boolean isHttps = useSecureRedirectUri();
        final Resolver resolver = this.auth.getResolver();
        if (resolver == null) {
            throw new ResolverMissingException("Resolver has not been set.");
        }
		final Call c = resolver.auth(getKey());

        final String overrideHost;
        if (getConfiguration().hasPath(SettingKeys.REDIRECT_URI_HOST)
                && !(overrideHost = getConfiguration().getString(SettingKeys.REDIRECT_URI_HOST)).trim().isEmpty()) {
            return "http" + (isHttps ? "s" : "") + "://" + overrideHost
					+ c.url();
		} else {
			return c.absoluteURL(request, isHttps);
		}
	}

	public ExternalAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle) {
		super(auth, lifecycle);
	}

	@Override
	public boolean isExternal() {
		return true;
	}

}

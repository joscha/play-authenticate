package com.feth.play.module.pa.providers.ext;

import java.util.Collections;
import java.util.List;

import play.Application;
import play.mvc.Call;
import play.mvc.Http.Request;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.AuthProvider;

public abstract class ExternalAuthProvider extends AuthProvider {

	private static abstract class SettingKeys {
		@Deprecated
		public static final String SECURE_REDIRECT_URI = "secureRedirectUri";
		public static final String REDIRECT_URI_HOST = "redirectUri.host";
		public static final String REDIRECT_URI_SECURE = "redirectUri.secure";
	}

	@Override
	protected List<String> neededSettingKeys() {
		return Collections.emptyList();
	}

	private boolean useSecureRedirectUri() {
		Boolean secure = getConfiguration().getBoolean(
				SettingKeys.REDIRECT_URI_SECURE);
		if (secure == null) {
			secure = getConfiguration().getBoolean(
					SettingKeys.SECURE_REDIRECT_URI);
		}
		if (secure == null) {
			return false;
		} else {
			return secure;
		}
	}

	protected String getRedirectUrl(final Request request) {
		final String overrideHost = getConfiguration().getString(
				SettingKeys.REDIRECT_URI_HOST);
		final boolean isHttps = useSecureRedirectUri();
		final Call c = PlayAuthenticate.getResolver().auth(getKey());
		if (overrideHost != null && !overrideHost.trim().isEmpty()) {
			return "http" + (isHttps ? "s" : "") + "://" + overrideHost
					+ c.url();
		} else {
			return c.absoluteURL(request, isHttps);
		}
	}

	public ExternalAuthProvider(Application app) {
		super(app);
	}

	@Override
	public boolean isExternal() {
		return true;
	}

}

package com.feth.play.module.pa.providers.ext;

import java.util.Collections;
import java.util.List;

import play.Application;
import play.mvc.Http.Request;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.AuthProvider;

public abstract class ExternalAuthProvider extends AuthProvider {

	private static abstract class SettingKeys {
		public static final String SECURE_REDIRECT_URI = "secureRedirectUri";
	}

	@Override
	protected List<String> neededSettingKeys() {
		return Collections.singletonList(SettingKeys.SECURE_REDIRECT_URI);
	}

	private boolean useSecureRedirectUri() {
		return getConfiguration().getBoolean(SettingKeys.SECURE_REDIRECT_URI);
	}

	protected String getRedirectUrl(final Request request) {
		return PlayAuthenticate.getResolver().auth(getKey())
				.absoluteURL(request, useSecureRedirectUri());
	}

	public ExternalAuthProvider(Application app) {
		super(app);
	}

	@Override
	public boolean isExternal() {
		return true;
	}

}

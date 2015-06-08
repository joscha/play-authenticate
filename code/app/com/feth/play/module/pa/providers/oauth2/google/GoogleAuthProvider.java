package com.feth.play.module.pa.providers.oauth2.google;

import play.Application;
import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.google.inject.Inject;

public class GoogleAuthProvider extends
		OAuth2AuthProvider<GoogleAuthUser, GoogleAuthInfo> {

	public static final String PROVIDER_KEY = "google";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";

	@Inject
	public GoogleAuthProvider(Application app) {
		super(app);
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	protected GoogleAuthUser transform(final GoogleAuthInfo info, final String state)
			throws AuthException {

		final String url = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);
		final WSResponse r = WS
				.url(url)
				.setQueryParameter(OAuth2AuthProvider.Constants.ACCESS_TOKEN,
						info.getAccessToken()).get()
				.get(getTimeout());

		final JsonNode result = r.asJson();
		if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
			throw new AuthException(result.get(
					OAuth2AuthProvider.Constants.ERROR).asText());
		} else {
			Logger.debug(result.toString());
			return new GoogleAuthUser(result, info, state);
		}
	}

	@Override
	protected GoogleAuthInfo buildInfo(final WSResponse r)
			throws AccessTokenException {
		final JsonNode n = r.asJson();
		Logger.debug(n.toString());

		if (n.get(OAuth2AuthProvider.Constants.ERROR) != null) {
			throw new AccessTokenException(n.get(
					OAuth2AuthProvider.Constants.ERROR).asText());
		} else {
			return new GoogleAuthInfo(n);
		}
	}

}

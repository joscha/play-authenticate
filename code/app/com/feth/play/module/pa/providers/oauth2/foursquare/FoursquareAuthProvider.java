package com.feth.play.module.pa.providers.oauth2.foursquare;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FoursquareAuthProvider extends
		OAuth2AuthProvider<FoursquareAuthUser, FoursquareAuthInfo> {

	public static final String PROVIDER_KEY = "foursquare";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
	private static final String OAUTH_TOKEN = "oauth_token";
	private static final String VERSION = "20120617";

	@Inject
	public FoursquareAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final WSClient wsClient) {
		super(auth, lifecycle, wsClient);
	}

	@Override
	protected FoursquareAuthInfo buildInfo(final WSResponse r)
			throws AccessTokenException {

		if (r.getStatus() >= 400) {
			throw new AccessTokenException(r.toString());
		} else {
			final JsonNode result = r.asJson();
			Logger.debug(result.asText());
			return new FoursquareAuthInfo(result.get(
					OAuth2AuthProvider.Constants.ACCESS_TOKEN).asText());
		}
	}

	@Override
	protected AuthUserIdentity transform(final FoursquareAuthInfo info, final String state)
			throws AuthException {

		final String url = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);

		final WSResponse r = fetchAuthResponse(url,
				new QueryParam(OAUTH_TOKEN, info.getAccessToken()),
				new QueryParam("v", VERSION));

		final JsonNode result = r.asJson();
		if (r.getStatus() >= 400) {
			throw new AuthException(result.get("meta").get("errorDetail").asText());
		} else {
			Logger.debug(result.toString());
			return new FoursquareAuthUser(result.get("response").get("user"), info, state);
		}
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

}

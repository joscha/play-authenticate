package com.feth.play.module.pa.providers.oauth2.foursquare;

import org.codehaus.jackson.JsonNode;

import play.Application;
import play.Logger;
import play.libs.WS;
import play.libs.WS.Response;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthUserIdentity;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;

public class FoursquareAuthProvider extends
		OAuth2AuthProvider<FoursquareAuthUser, FoursquareAuthInfo> {

	public static final String PROVIDER_KEY = "4square";
	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
	private static final String OAUTH_TOKEN = "oauth_token";

	public FoursquareAuthProvider(Application app) {
		super(app);
	}

	@Override
	protected FoursquareAuthInfo buildInfo(final Response r)
			throws AccessTokenException {

		if (r.getStatus() >= 400) {
			throw new AccessTokenException(r.getStatusText());
		} else {
			final JsonNode result = r.asJson();
			Logger.debug(result.asText());
			return new FoursquareAuthInfo(result.get(
					OAuth2AuthProvider.Constants.ACCESS_TOKEN).asText());
		}
	}

	@Override
	protected AuthUserIdentity transform(final FoursquareAuthInfo info)
			throws AuthException {
		final String url = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);
		final Response r = WS
				.url(url)
				.setQueryParameter(OAUTH_TOKEN,
						info.getAccessToken()).get()
				.get(PlayAuthenticate.TIMEOUT);

		final JsonNode result = r.asJson();
		if (r.getStatus() >= 400) {
			throw new AuthException(result.get("meta").get("errorDetail").asText());
		} else {
			Logger.debug(result.toString());
			return new FoursquareAuthUser(result.get("response").get("user"), info);
		}
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

}

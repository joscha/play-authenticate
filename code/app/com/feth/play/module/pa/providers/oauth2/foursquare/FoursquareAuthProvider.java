package com.feth.play.module.pa.providers.oauth2.foursquare;

import org.codehaus.jackson.JsonNode;

import play.Application;
import play.Logger;
import play.libs.WS;
import play.libs.WS.Response;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;

public class FoursquareAuthProvider extends
		OAuth2AuthProvider<FoursquareAuthUser, FoursquareAuthInfo> {

	static final String PROVIDER_KEY = "foursquare";
	
	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
	private static final String OAUTH_TOKEN = "oauth_token";
	private static final String VERSION = "20120617";

	public FoursquareAuthProvider(Application app) {
		super(app);
	}

	@Override
	protected FoursquareAuthInfo buildInfo(final Response r)
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
		final Response r = WS
				.url(url)
				.setQueryParameter(OAUTH_TOKEN,
						info.getAccessToken())
				.setQueryParameter("v", VERSION)
				.get()
				.get(PlayAuthenticate.TIMEOUT);

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

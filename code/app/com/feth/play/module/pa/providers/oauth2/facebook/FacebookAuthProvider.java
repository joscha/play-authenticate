package com.feth.play.module.pa.providers.oauth2.facebook;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.codehaus.jackson.JsonNode;

import play.Application;
import play.Logger;
import play.libs.WS;
import play.libs.WS.Response;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;

public class FacebookAuthProvider extends
		OAuth2AuthProvider<FacebookAuthUser, FacebookAuthInfo> {

	private static final String MESSAGE = "message";

	static final String PROVIDER_KEY = "facebook";
	
	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";

	public FacebookAuthProvider(Application app) {
		super(app);
	}

	@Override
	protected FacebookAuthUser transform(FacebookAuthInfo info, final String state)
			throws AuthException {

		final String url = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);
		final Response r = WS
				.url(url)
				.setQueryParameter(OAuth2AuthProvider.Constants.ACCESS_TOKEN,
						info.getAccessToken()).get()
				.get(PlayAuthenticate.TIMEOUT);

		final JsonNode result = r.asJson();
		if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
			throw new AuthException(result.get(MESSAGE).asText());
		} else {
			Logger.debug(result.toString());
			return new FacebookAuthUser(result, info, state);
		}
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	protected FacebookAuthInfo buildInfo(final Response r)
			throws AccessTokenException {
		if (r.getStatus() >= 400) {
			throw new AccessTokenException(r.asJson().get(MESSAGE).asText());
		} else {
			final String query = r.getBody();
			Logger.debug(query);
			final List<NameValuePair> pairs = URLEncodedUtils.parse(
					URI.create("/?" + query), "utf-8");
			if (pairs.size() < 2) {
				throw new AccessTokenException();
			}
			final Map<String, String> m = new HashMap<String, String>(
					pairs.size());
			for (final NameValuePair nameValuePair : pairs) {
				m.put(nameValuePair.getName(), nameValuePair.getValue());
			}

			return new FacebookAuthInfo(m);
		}
	}

}

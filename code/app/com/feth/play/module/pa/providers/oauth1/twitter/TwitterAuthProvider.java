package com.feth.play.module.pa.providers.oauth1.twitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth1.OAuth1AuthProvider;
import com.google.inject.Inject;
import play.inject.ApplicationLifecycle;
import play.libs.oauth.OAuth.OAuthCalculator;
import play.libs.oauth.OAuth.RequestToken;
import play.libs.ws.WSClient;
import play.mvc.Http;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class TwitterAuthProvider extends
		OAuth1AuthProvider<TwitterAuthUser, TwitterAuthInfo> {

	public static final String PROVIDER_KEY = "twitter";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
    private static final String DENIED_KEY = "denied";

	@Inject
	public TwitterAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final WSClient wsClient) {
		super(auth, lifecycle, wsClient);
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	protected List<String> neededSettingKeys() {
		final List<String> neededSettingKeys = super.neededSettingKeys();
		neededSettingKeys.add(USER_INFO_URL_SETTING_KEY);
		return neededSettingKeys;
	}

	@Override
	protected TwitterAuthUser transform(final TwitterAuthInfo info)
			throws AuthException {
		final String userInfoUrl = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);

		final OAuthCalculator op = getOAuthCalculator(info);

		final JsonNode userJson = signedOauthGet(userInfoUrl, op);

		return new TwitterAuthUser(userJson, info);
	}

	@Override
	protected TwitterAuthInfo buildInfo(final RequestToken rtoken)
			throws AccessTokenException {
		return new TwitterAuthInfo(rtoken.token, rtoken.secret);
	}

    @Override
    protected void checkError(Http.Request request) throws AuthException {
        final String error = request.getQueryString(DENIED_KEY);

        if (error != null) {
            throw new AccessDeniedException(getKey());
        }
    }

}

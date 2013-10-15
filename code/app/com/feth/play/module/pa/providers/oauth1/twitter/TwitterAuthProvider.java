package com.feth.play.module.pa.providers.oauth1.twitter;

import java.util.List;

import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import org.codehaus.jackson.JsonNode;

import play.Application;
import play.api.libs.oauth.OAuthCalculator;
import play.api.libs.oauth.RequestToken;

import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth1.OAuth1AuthProvider;
import play.mvc.Http;

public class TwitterAuthProvider extends
		OAuth1AuthProvider<TwitterAuthUser, TwitterAuthInfo> {

	static final String PROVIDER_KEY = "twitter";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
    private static final String DENIED_KEY = "denied";

	public TwitterAuthProvider(final Application app) {
		super(app);
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
		return new TwitterAuthInfo(rtoken.token(), rtoken.secret());
	}

    @Override
    protected void checkError(Http.Request request) throws AuthException {
        final String error = Authenticate.getQueryString(request,
                DENIED_KEY);

        if (error != null) {
            throw new AccessDeniedException(getKey());
        }
    }

}

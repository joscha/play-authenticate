package com.feth.play.module.pa.providers.oauth1.linkedin;

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

public class LinkedinAuthProvider extends
		OAuth1AuthProvider<LinkedinAuthUser, LinkedinAuthInfo> {

	static final String PROVIDER_KEY = "linkedin";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
	private static final String USER_EMAIL_URL_SETTING_KEY = "userEmailUrl";

    public static final String OAUTH_ACCESS_DENIED= "user_refused";


	public LinkedinAuthProvider(final Application app) {
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
		neededSettingKeys.add(USER_EMAIL_URL_SETTING_KEY);
		return neededSettingKeys;
	}

	@Override
	protected LinkedinAuthUser transform(final LinkedinAuthInfo info)
			throws AuthException {
		final String userInfoUrl = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);
		final String emailUrl = getConfiguration().getString(
				USER_EMAIL_URL_SETTING_KEY);

		final OAuthCalculator op = getOAuthCalculator(info);

		final JsonNode userJson = signedOauthGet(userInfoUrl, op);
		final JsonNode emailJson = signedOauthGet(emailUrl, op);

		return new LinkedinAuthUser(userJson, emailJson.asText(), info);
	}

	@Override
	protected LinkedinAuthInfo buildInfo(final RequestToken rtoken)
			throws AccessTokenException {
		return new LinkedinAuthInfo(rtoken.token(), rtoken.secret());
	}

    @Override
    protected void checkError(Http.Request request) throws AuthException {
        final String error = Authenticate.getQueryString(request,
                Constants.OAUTH_PROBLEM);

        if (error != null) {
            if (error.equals(OAUTH_ACCESS_DENIED)) {
                throw new AccessDeniedException(getKey());
            } else {
                throw new AuthException(error);
            }
        }
    }
}

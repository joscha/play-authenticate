package com.feth.play.module.pa.providers.oauth1.xing;

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

/**
 * Auth provider for business social network <a
 * href="https://www.xing.com/">XING</a>.
 */
@Singleton
public class XingAuthProvider extends
		OAuth1AuthProvider<XingAuthUser, XingAuthInfo> {

	public static final String PROVIDER_KEY = "xing";

	private static final String NODE_USERS = "users";
	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
	private static final String XING_ERROR = "xing_error";
	private static final String ACCESS_DENIED = "user_abort";

	@Inject
	public XingAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final WSClient wsClient) {
		super(auth, lifecycle, wsClient);
	}

	@Override
	protected XingAuthInfo buildInfo(RequestToken rtoken)
			throws AccessTokenException {
		return new XingAuthInfo(rtoken.token, rtoken.secret);
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
	protected XingAuthUser transform(final XingAuthInfo info)
			throws AuthException {
		final String userInfoUrl = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);

		final OAuthCalculator op = getOAuthCalculator(info);

		final JsonNode userJson = signedOauthGet(userInfoUrl, op);

		return new XingAuthUser(userJson.path(NODE_USERS).get(0), info);
	}

    @Override
    protected void checkError(Http.Request request) throws AuthException {
        final String error = request.getQueryString(XING_ERROR);

        if (error != null) {
            if (error.equals(ACCESS_DENIED)) {
                throw new AccessDeniedException(getKey());
            } else {
                throw new AuthException(error);
            }
        }
    }

}

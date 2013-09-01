package com.feth.play.module.pa.providers.oauth1.xing;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;

import play.Application;
import play.api.libs.oauth.OAuthCalculator;
import play.api.libs.oauth.RequestToken;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth1.OAuth1AuthProvider;

/**
 * Auth provider for business social network <a
 * href="https://www.xing.com/">XING</a>.
 */
public class XingAuthProvider extends
		OAuth1AuthProvider<XingAuthUser, XingAuthInfo> {

	static final String PROVIDER_KEY = "xing";

	private static final String NODE_USERS = "users";
	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
	private static final String XING_ERROR = "xing_error";
	private static final Object ACCESS_DENIED = "user_abort";

	public XingAuthProvider(final Application app) {
		super(app);
	}

	@Override
	protected XingAuthInfo buildInfo(RequestToken rtoken)
			throws AccessTokenException {
		return new XingAuthInfo(rtoken.token(), rtoken.secret());
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
	public Object authenticate(final Context context, final Object payload)
			throws AuthException {
		// Check whether we got an error in the request
		final Request request = context.request();
		final String error = request.getQueryString(XING_ERROR);
		if (StringUtils.isNotBlank(error)) {
			if (error.equals(ACCESS_DENIED)) {
				throw new AccessDeniedException(getKey());
			} else {
				throw new AuthException(error);
			}
		}
		// Everything OK – back to normal workflow
		return super.authenticate(context, payload);
	}
}

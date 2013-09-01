package com.feth.play.module.pa.providers.oauth1.xing;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import play.Application;
import play.Configuration;
import play.api.libs.json.JsValue;
import play.api.libs.oauth.ConsumerKey;
import play.api.libs.oauth.OAuthCalculator;
import play.api.libs.oauth.RequestToken;
import play.api.libs.ws.Response;
import play.api.libs.ws.WS;
import play.libs.Json;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import scala.concurrent.Future;

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

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
	private static final String ERROR = "xing_error";
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
		List<String> neededSettingKeys = super.neededSettingKeys();
		Collections.addAll(neededSettingKeys, USER_INFO_URL_SETTING_KEY);
		return neededSettingKeys;
	}

	@Override
	protected XingAuthUser transform(XingAuthInfo identity)
			throws AuthException {
		final String url = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);

		final RequestToken token = new RequestToken(identity.getAccessToken(),
				identity.getAccessTokenSecret());
		final Configuration c = getConfiguration();
		final ConsumerKey cK = new ConsumerKey(
				c.getString(SettingKeys.CONSUMER_KEY),
				c.getString(SettingKeys.CONSUMER_SECRET));

		final OAuthCalculator calculator = new OAuthCalculator(cK, token);

		final Future<Response> future = WS.url(url).sign(calculator).get();
		final play.api.libs.ws.Response response = new play.libs.F.Promise<play.api.libs.ws.Response>(
				future).get();
		final JsValue json = response.json();

		return new XingAuthUser(Json.parse(json.toString()).path("users")
				.get(0), identity);
	}

	@Override
	public Object authenticate(Context context, Object payload)
			throws AuthException {
		// Check whether we got an error in the request
		final Request request = context.request();
		final String error = request.getQueryString(ERROR);
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

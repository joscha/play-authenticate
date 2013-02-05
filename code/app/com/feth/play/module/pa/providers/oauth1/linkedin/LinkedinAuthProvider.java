package com.feth.play.module.pa.providers.oauth1.linkedin;

import play.Application;
import play.Configuration;
import play.api.libs.json.JsValue;
import play.api.libs.oauth.ConsumerKey;
import play.api.libs.oauth.OAuthCalculator;
import play.api.libs.oauth.RequestToken;
import play.api.libs.ws.Response;
import play.api.libs.ws.WS;
import play.libs.Json;
import scala.concurrent.Future;

import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth1.OAuth1AuthProvider;

public class LinkedinAuthProvider extends
		OAuth1AuthProvider<LinkedinAuthUser, LinkedinAuthInfo> {

	static final String PROVIDER_KEY = "linkedin";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
	private static final String USER_EMAIL_URL_SETTING_KEY = "userEmailUrl";

	public LinkedinAuthProvider(final Application app) {
		super(app);
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	protected LinkedinAuthUser transform(final LinkedinAuthInfo info)
			throws AuthException {
		final String url = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);
		final String urlEmail = getConfiguration().getString(
				USER_EMAIL_URL_SETTING_KEY);

		final RequestToken token = new RequestToken(info.getAccessToken(),
				info.getAccessTokenSecret());
		final Configuration c = getConfiguration();
		final ConsumerKey cK = new ConsumerKey(
				c.getString(SettingKeys.CONSUMER_KEY),
				c.getString(SettingKeys.CONSUMER_SECRET));

		final OAuthCalculator op = new OAuthCalculator(cK, token);


		final Future<Response> resp = WS.url(url).sign(op).get();
		final Future<play.api.libs.ws.Response> future = WS.url(url).sign(op).get();
		play.api.libs.ws.Response response = new play.libs.F.Promise<play.api.libs.ws.Response>(future).get();
		final JsValue json = response.json();

		final Future<Response> respEmail = WS.url(urlEmail).sign(op).get();
		final Future<play.api.libs.ws.Response> futureEmail = WS.url(urlEmail).sign(op).get();
		play.api.libs.ws.Response responseEmail = new play.libs.F.Promise<play.api.libs.ws.Response>(futureEmail).get();
		final JsValue jsonEmail = responseEmail.json();
		final String email = Json.parse(jsonEmail.toString()).asText();

/*

		final Promise<play.api.libs.ws.Response> promise = WS.url(url).sign(op)
				.get();
		final Promise<play.api.libs.ws.Response> promiseEmail = WS
				.url(urlEmail).sign(op).get();

		final JsValue json = promise.value().get().json();
		final JsValue jsonEmail = promiseEmail.value().get().json();

		

*/

		return new LinkedinAuthUser(Json.parse(json.toString()), email, info);
	}

	@Override
	protected LinkedinAuthInfo buildInfo(final RequestToken rtoken)
			throws AccessTokenException {
		return new LinkedinAuthInfo(rtoken.token(), rtoken.secret());
	}

}

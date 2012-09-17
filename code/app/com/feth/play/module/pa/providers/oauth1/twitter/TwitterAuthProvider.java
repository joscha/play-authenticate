package com.feth.play.module.pa.providers.oauth1.twitter;

import play.Application;
import play.Configuration;
import play.api.libs.json.JsValue;
import play.api.libs.oauth.ConsumerKey;
import play.api.libs.oauth.OAuthCalculator;
import play.api.libs.oauth.RequestToken;
import play.api.libs.ws.Response;
import play.api.libs.ws.WS;
import play.libs.Json;
import scala.Either;
import scala.concurrent.Future;

import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth1.OAuth1AuthProvider;

public class TwitterAuthProvider extends
		OAuth1AuthProvider<TwitterAuthUser, TwitterAuthInfo> {

	static final String PROVIDER_KEY = "twitter";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";

	public TwitterAuthProvider(final Application app) {
		super(app);
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	protected TwitterAuthUser transform(final TwitterAuthInfo info)
			throws AuthException {
		final String url = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);

		final RequestToken token = new RequestToken(info.getAccessToken(),
				info.getAccessTokenSecret());
		final Configuration c = getConfiguration();
		final ConsumerKey cK = new ConsumerKey(
				c.getString(SettingKeys.CONSUMER_KEY),
				c.getString(SettingKeys.CONSUMER_SECRET));

		final OAuthCalculator op = new OAuthCalculator(cK, token);

		final Future<Response> resp = WS.url(url).sign(op).get();

		final Either<Throwable, Response> either = resp.value().get();
		if (either.isLeft()) {
			final Throwable t = either.left().get();
			if (t.getMessage() == null) {
				throw new AuthException();
			} else {
				throw new AuthException(t.getMessage());
			}
		}

		final JsValue json = either.right().get().json();
		return new TwitterAuthUser(Json.parse(json.toString()), info);
	}

	@Override
	protected TwitterAuthInfo buildInfo(final RequestToken rtoken)
			throws AccessTokenException {
		return new TwitterAuthInfo(rtoken.token(), rtoken.secret());
	}

}

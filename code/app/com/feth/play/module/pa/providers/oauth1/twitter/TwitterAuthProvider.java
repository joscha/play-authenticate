package com.feth.play.module.pa.providers.oauth1.twitter;

import play.Application;
import play.Configuration;
import play.api.libs.concurrent.Promise;
import play.api.libs.json.JsValue;
import play.api.libs.oauth.ConsumerKey;
import play.api.libs.oauth.OAuthCalculator;
import play.api.libs.oauth.RequestToken;
import play.api.libs.ws.WS;
import play.libs.Json;

import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth1.OAuth1AuthInfo;
import com.feth.play.module.pa.providers.oauth1.OAuth1AuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;

public class TwitterAuthProvider extends OAuth1AuthProvider<TwitterAuthUser> {

	static final String PROVIDER_KEY = "twitter";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";

	public TwitterAuthProvider(Application app) {
		super(app);
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	protected AuthUserIdentity transform(OAuth1AuthInfo info)
			throws AuthException {
		final String url = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);

		RequestToken token = new RequestToken(info.getAccessToken(),
				info.getAccessTokenSecret());
		final Configuration c = getConfiguration();
		ConsumerKey cK = new ConsumerKey(c.getString(SettingKeys.CLIENT_ID),
				c.getString(SettingKeys.CLIENT_SECRET));
		OAuthCalculator op = new OAuthCalculator(cK, token);

		Promise<play.api.libs.ws.Response> promise = WS.url(url).sign(op).get();

		JsValue json = promise.value().get().json();
		String stringJson = json.toString();

		return new TwitterAuthUser(Json.parse(stringJson), info);
	}

}

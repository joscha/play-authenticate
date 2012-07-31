package com.feth.play.module.pa.providers.oauth1;

import java.util.ArrayList;
import java.util.List;

import oauth.signpost.exception.OAuthException;
import play.Application;
import play.Configuration;
import play.Logger;
import play.api.libs.oauth.ConsumerKey;
import play.api.libs.oauth.OAuth;
import play.api.libs.oauth.RequestToken;
import play.api.libs.oauth.ServiceInfo;
import play.cache.Cache;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import scala.Either;
import scala.Either.RightProjection;

import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.ext.ExternalAuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;

public abstract class OAuth1AuthProvider<U extends AuthUserIdentity> extends
		ExternalAuthProvider {

	public OAuth1AuthProvider(final Application app) {
		super(app);
	}

	@Override
	protected List<String> neededSettingKeys() {
		final List<String> ret = new ArrayList<String>();
		ret.addAll(super.neededSettingKeys());
		ret.add(SettingKeys.ACCESS_TOKEN_URL);
		ret.add(SettingKeys.AUTHORIZATION_URL);
		ret.add(SettingKeys.REQUEST_TOKEN_URL);
		ret.add(SettingKeys.CONSUMER_KEY);
		ret.add(SettingKeys.CONSUMER_SECRET);
		return ret;
	}

	public static abstract class SettingKeys {
		public static final String REQUEST_TOKEN_URL = "requestTokenUrl";
		public static final String AUTHORIZATION_URL = "authorizationUrl";
		public static final String ACCESS_TOKEN_URL = "accessTokenUrl";
		public static final String CONSUMER_KEY = "consumerKey";
		public static final String CONSUMER_SECRET = "consumerSecret";
	}

	public static abstract class Constants {
		public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
		public static final String OAUTH_TOKEN = "oauth_token";
		public static final String OAUTH_VERIFIER = "oauth_verifier";
		public static final String CACHE_TOKEN = "token";

	}

	@Override
	public Object authenticate(final Context context, final Object payload)
			throws AuthException {

		final Request request = context.request();
		String uri = request.uri();

		if (Logger.isDebugEnabled()) {
			Logger.debug("Returned with URL: '" + uri + "'");
		}

		final Configuration c = getConfiguration();

		ConsumerKey key = new ConsumerKey(c.getString(SettingKeys.CLIENT_ID),
				c.getString(SettingKeys.CLIENT_SECRET));
		String requestTokenURL = c.getString(SettingKeys.REQUEST_TOKEN_URL);
		String accessTokenURL = c.getString(SettingKeys.ACCESS_TOKEN_URL);
		String authorizationURL = c.getString(SettingKeys.AUTHORIZATION_URL);
		ServiceInfo info = new ServiceInfo(requestTokenURL, accessTokenURL,
				authorizationURL, key);
		OAuth service = new OAuth(info, true);

		if (uri.contains(Constants.OAUTH_VERIFIER)) {

			RequestToken rtoken = (RequestToken) Cache
					.get(Constants.CACHE_TOKEN);
			String verifier = Authenticate.getQueryString(request,
					Constants.OAUTH_VERIFIER);
			Either<OAuthException, RequestToken> retrieveAccessToken = service
					.retrieveAccessToken(rtoken, verifier);

			if (retrieveAccessToken.isLeft()) {
				throw new AuthException(retrieveAccessToken.left().get()
						.getLocalizedMessage());
			} else {
				RightProjection<OAuthException, RequestToken> right = retrieveAccessToken
						.right();

				final String token = right.get().token();
				final String secret = right.get().secret();

				OAuth1AuthInfo I = new OAuth1AuthInfo(token, secret);
				final AuthUserIdentity u = transform(I);
				return u;

			}
		} else {

			String callbackURL = getAbsoluteUrl(request);

			Either<OAuthException, RequestToken> reponse = service
					.retrieveRequestToken(callbackURL);

			if (reponse.isLeft()) {
				throw new AuthException(reponse.left().get()
						.getLocalizedMessage());
			} else {
				RightProjection<OAuthException, RequestToken> right = reponse
						.right();

				final String token = right.get().token();
				String redirectUrl = service.redirectUrl(token);

				Cache.set(Constants.CACHE_TOKEN, right.get());
				return redirectUrl;
			}
		}

	}

	/**
	 * This allows custom implementations to enrich an AuthUser object or
	 * provide their own implementaion
	 * 
	 * @param i
	 * @param state
	 * @return
	 * @throws AuthException
	 */
	protected abstract AuthUserIdentity transform(final OAuth1AuthInfo i)
			throws AuthException;
}

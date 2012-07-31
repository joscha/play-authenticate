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
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import scala.Either;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.ext.ExternalAuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;

public abstract class OAuth1AuthProvider<U extends AuthUserIdentity, I extends OAuth1AuthInfo>
		extends ExternalAuthProvider {

	private static final String CACHE_TOKEN = "pa.oauth1.rtoken";

	public OAuth1AuthProvider(final Application app) {
		super(app);
	}

	protected abstract I buildInfo(final RequestToken rtoken)
			throws AccessTokenException;

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
	}

	@Override
	public Object authenticate(final Context context, final Object payload)
			throws AuthException {

		final Request request = context.request();
		final String uri = request.uri();

		if (Logger.isDebugEnabled()) {
			Logger.debug("Returned with URL: '" + uri + "'");
		}

		final Configuration c = getConfiguration();

		final ConsumerKey key = new ConsumerKey(
				c.getString(SettingKeys.CONSUMER_KEY),
				c.getString(SettingKeys.CONSUMER_SECRET));
		final String requestTokenURL = c
				.getString(SettingKeys.REQUEST_TOKEN_URL);
		final String accessTokenURL = c.getString(SettingKeys.ACCESS_TOKEN_URL);
		final String authorizationURL = c
				.getString(SettingKeys.AUTHORIZATION_URL);
		final ServiceInfo info = new ServiceInfo(requestTokenURL,
				accessTokenURL, authorizationURL, key);
		final OAuth service = new OAuth(info, true);

		if (uri.contains(Constants.OAUTH_VERIFIER)) {

			final RequestToken rtoken = (RequestToken) PlayAuthenticate
					.removeFromCache(context.session(), CACHE_TOKEN);
			final String verifier = Authenticate.getQueryString(request,
					Constants.OAUTH_VERIFIER);
			final Either<OAuthException, RequestToken> retrieveAccessToken = service
					.retrieveAccessToken(rtoken, verifier);

			if (retrieveAccessToken.isLeft()) {
				throw new AuthException(retrieveAccessToken.left().get()
						.getLocalizedMessage());
			} else {
				final I i = buildInfo(retrieveAccessToken.right().get());
				return transform(i);
			}
		} else {

			final String callbackURL = getRedirectUrl(request);

			final Either<OAuthException, RequestToken> reponse = service
					.retrieveRequestToken(callbackURL);

			if (reponse.isLeft()) {
				// Exception happened
				throw new AuthException(reponse.left().get()
						.getLocalizedMessage());
			} else {
				// All good, we have the request token
				final RequestToken rtoken = reponse.right().get();

				final String token = rtoken.token();
				final String redirectUrl = service.redirectUrl(token);

				PlayAuthenticate.storeInCache(context.session(), CACHE_TOKEN,
						rtoken);
				return redirectUrl;
			}
		}

	}

	/**
	 * This allows custom implementations to enrich an AuthUser object or
	 * provide their own implementation
	 * 
	 * @param i
	 * @param state
	 * @return
	 * @throws AuthException
	 */
	protected abstract U transform(final I identity) throws AuthException;
}

package com.feth.play.module.pa.providers.oauth1;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.ext.ExternalAuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.oauth.OAuth;
import play.libs.oauth.OAuth.ConsumerKey;
import play.libs.oauth.OAuth.OAuthCalculator;
import play.libs.oauth.OAuth.RequestToken;
import play.libs.oauth.OAuth.ServiceInfo;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class OAuth1AuthProvider<U extends AuthUserIdentity, I extends OAuth1AuthInfo>
		extends ExternalAuthProvider {

	private static final String CACHE_TOKEN = "pa.oauth1.rtoken";

	private final WSClient wsClient;

	public OAuth1AuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final WSClient wsClient) {
		super(auth, lifecycle);
		this.wsClient = wsClient;
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
		public static final String OAUTH_PROBLEM = "oauth_problem";
        public static final String OAUTH_ACCESS_DENIED = "access_denied";
	}

	public static class SerializableRequestToken extends RequestToken implements Serializable {
		private static final long serialVersionUID = 1L;

		public SerializableRequestToken(RequestToken source) {
			super(source.token, source.secret);
		}
	}

    protected void checkError(Request request) throws AuthException{
        final String error = request.getQueryString(Constants.OAUTH_PROBLEM);

        if (error != null) {
            if (error.equals(Constants.OAUTH_ACCESS_DENIED)) {
                throw new AccessDeniedException(getKey());
            } else {
                throw new AuthException(error);
            }
        }
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

        checkError(request);

        if (uri.contains(Constants.OAUTH_VERIFIER)) {

			final RequestToken rtoken = (RequestToken) this.auth
					.removeFromCache(context.session(), CACHE_TOKEN);
			final String verifier = request.getQueryString(Constants.OAUTH_VERIFIER);
			try {
				final RequestToken response = service
						.retrieveAccessToken(rtoken, verifier);
				final I i = buildInfo(response);
				return transform(i);
			} catch (RuntimeException ex) {
				throw new AuthException(ex
						.getLocalizedMessage());
			}
		} else {

			final String callbackURL = getRedirectUrl(request);

			try {
				final RequestToken response = service
						.retrieveRequestToken(callbackURL);
				// All good, we have the request token
				final String token = response.token;
				final String redirectUrl = service.redirectUrl(token);

				this.auth.storeInCache(context.session(), CACHE_TOKEN,
						new SerializableRequestToken(response));
				return redirectUrl;
			} catch (RuntimeException ex) {
				// Exception happened
				throw new AuthException(ex
						.getLocalizedMessage());
			}
		}

	}

	protected JsonNode signedOauthGet(final String url, final OAuthCalculator calculator) throws AuthException {
		final Future<WSResponse> oathFuture = wsClient.url(url).sign(calculator).get().toCompletableFuture();

		try {
			final WSResponse response = oathFuture.get(getTimeout(), MILLISECONDS);
			return response.asJson();
		} catch(InterruptedException | ExecutionException | TimeoutException e) {
			throw new AuthException(e.getMessage(), e);
		}
	}

	protected OAuthCalculator getOAuthCalculator(final OAuth1AuthInfo info) {
		final RequestToken token = new RequestToken(info.getAccessToken(),
				info.getAccessTokenSecret());
		final Configuration c = getConfiguration();
		final ConsumerKey cK = new ConsumerKey(
				c.getString(SettingKeys.CONSUMER_KEY),
				c.getString(SettingKeys.CONSUMER_SECRET));

        return new OAuthCalculator(cK, token);
	}

	/**
	 * This allows custom implementations to enrich an AuthUser object or
	 * provide their own implementation
	 *
	 * @return
	 * @throws AuthException
	 */
	protected abstract U transform(final I identity) throws AuthException;
}

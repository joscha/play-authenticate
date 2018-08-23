package com.feth.play.module.pa.providers.oauth2;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.*;
import com.feth.play.module.pa.providers.ext.ExternalAuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.typesafe.config.Config;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.i18n.MessagesApi;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Http.Session;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class OAuth2AuthProvider<U extends AuthUserIdentity, I extends OAuth2AuthInfo>
		extends ExternalAuthProvider {

	protected class QueryParam {
		private String param;
		private String value;

		public QueryParam(String param, String value) {
			this.param = param;
			this.value = value;
		}
	}

    private static final String STATE_TOKEN = "pa.oauth2.state";
    protected static final String CONTENT_TYPE = "Content-Type";

	protected final WSClient wsClient;
	private final MessagesApi messagesApi;

	public OAuth2AuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final WSClient wsClient, final MessagesApi messagesApi) {
		super(auth, lifecycle);
		this.wsClient = wsClient;
		this.messagesApi = messagesApi;
	}

	@Override
	protected List<String> neededSettingKeys() {
		final List<String> ret = new ArrayList<String>();
		ret.addAll(super.neededSettingKeys());
		ret.add(SettingKeys.ACCESS_TOKEN_URL);
		ret.add(SettingKeys.AUTHORIZATION_URL);
		ret.add(SettingKeys.CLIENT_ID);
		ret.add(SettingKeys.CLIENT_SECRET);
		return ret;
	}

	public static abstract class SettingKeys {
		public static final String AUTHORIZATION_URL = "authorizationUrl";
		public static final String ACCESS_TOKEN_URL = "accessTokenUrl";
		public static final String CLIENT_ID = "clientId";
		public static final String CLIENT_SECRET = "clientSecret";
		public static final String SCOPE = "scope";
		public static final String ACCESS_TYPE = "accessType";
		public static final String APPROVAL_PROMPT = "approvalPrompt";
	}

	public static abstract class Constants {
		public static final String CLIENT_ID = "client_id";
		public static final String CLIENT_SECRET = "client_secret";
		public static final String REDIRECT_URI = "redirect_uri";
		public static final String SCOPE = "scope";
		public static final String ACCESS_TYPE = "access_type";
		public static final String APPROVAL_PROMPT = "approval_prompt";
		public static final String RESPONSE_TYPE = "response_type";
		public static final String STATE = "state";
		public static final String GRANT_TYPE = "grant_type";
		public static final String AUTHORIZATION_CODE = "authorization_code";
		public static final String ACCESS_TOKEN = "access_token";
		public static final String ERROR = "error";
		public static final String CODE = "code";
		public static final String TOKEN_TYPE = "token_type";
		public static final String EXPIRES_IN = "expires_in";
		public static final String REFRESH_TOKEN = "refresh_token";
		public static final String ACCESS_DENIED = "access_denied";
		public static final String REDIRECT_URI_MISMATCH = "redirect_uri_mismatch";
	}

	protected WSResponse fetchAuthResponse(String url, QueryParam...params) throws AuthException {
		final List<QueryParam> queryParams = Arrays.asList(params);
		final WSRequest request = wsClient.url(url);
		for(QueryParam param : queryParams) {
			request.addQueryParameter(param.param, param.value);
		}

		try {
			return request.get()
					.toCompletableFuture()
					.get(getTimeout(), MILLISECONDS);
		} catch(InterruptedException | ExecutionException | TimeoutException e) {
			throw new AuthException(e.getMessage(), e);
		}
	}

	protected String getAccessTokenParams(final Config c,
			final String code, Request request) throws ResolverMissingException {
		final List<NameValuePair> params = getParams(request, c);
		params.add(new BasicNameValuePair(Constants.CLIENT_SECRET, c
				.getString(SettingKeys.CLIENT_SECRET)));
		params.add(new BasicNameValuePair(Constants.GRANT_TYPE,
				Constants.AUTHORIZATION_CODE));
		params.add(new BasicNameValuePair(Constants.CODE, code));

		return URLEncodedUtils.format(params, "UTF-8");
	}

    protected Map<String, String> getHeaders() {
        return Collections.<String, String>emptyMap();
    }

	protected I getAccessToken(final String code, final Request request)
            throws AccessTokenException, ResolverMissingException {
		final Config c = getConfiguration();
		final String params = getAccessTokenParams(c, code, request);
		final String url = c.getString(SettingKeys.ACCESS_TOKEN_URL);
        final WSRequest wrh = wsClient.url(url);
        wrh.addHeader(CONTENT_TYPE, "application/x-www-form-urlencoded");
        for(final Map.Entry<String, String> header : getHeaders().entrySet()) {
            wrh.addHeader(header.getKey(), header.getValue());
        }

		try {
			final WSResponse r = wrh.post(params).toCompletableFuture().get(PlayAuthenticate.TIMEOUT, MILLISECONDS);
			return buildInfo(r);
		} catch(InterruptedException | ExecutionException | TimeoutException e) {
			throw new AccessTokenException(e.getMessage(), e);
		}
	}

	protected abstract I buildInfo(final WSResponse r)
			throws AccessTokenException;

	protected String getAuthUrl(final Request request, final String state)
			throws AuthException {
		final Config c = getConfiguration();
		final List<NameValuePair> params = getAuthParams(c, request, state);
		return generateURI(c.getString(SettingKeys.AUTHORIZATION_URL), params);
	}

	protected List<NameValuePair> getAuthParams(final Config c,
			final Request request, final String state) throws AuthException {
		final List<NameValuePair> params = getParams(request, c);
		if (c.hasPath(SettingKeys.SCOPE)) {
			params.add(new BasicNameValuePair(Constants.SCOPE, c
					.getString(SettingKeys.SCOPE)));
		}

		params.add(new BasicNameValuePair(Constants.RESPONSE_TYPE,
				Constants.CODE));

		if (c.hasPath(SettingKeys.ACCESS_TYPE)) {
			params.add(new BasicNameValuePair(Constants.ACCESS_TYPE, c
					.getString(SettingKeys.ACCESS_TYPE)));
		}

		if (c.hasPath(SettingKeys.APPROVAL_PROMPT)) {
			params.add(new BasicNameValuePair(Constants.APPROVAL_PROMPT, c
					.getString(SettingKeys.APPROVAL_PROMPT)));
		}

		if (state != null) {
			params.add(new BasicNameValuePair(Constants.STATE, state));
		}
		return params;
	}

	protected List<NameValuePair> getParams(final Request request,
			final Config c) throws ResolverMissingException {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(Constants.CLIENT_ID, c
				.getString(SettingKeys.CLIENT_ID)));
		params.add(new BasicNameValuePair(getRedirectUriKey(),
				getRedirectUrl(request)));
		return params;
	}

	protected String getRedirectUriKey() {
		return Constants.REDIRECT_URI;
	}

	@Override
	public Object authenticate(final Context context, final Object payload)
			throws AuthException {

		final Request request = context.request();

		if (Logger.isDebugEnabled()) {
			Logger.debug("Returned with URL: '" + request.uri() + "'");
		}

		final String error = request.getQueryString(getErrorParameterKey());

		if (error != null) {
			if (error.equals(Constants.ACCESS_DENIED)) {
				throw new AccessDeniedException(getKey());
			} else if (error.equals(Constants.REDIRECT_URI_MISMATCH)) {
				Logger.error("You must set the redirect URI for your provider to whatever you defined in your routes file."
						+ "For this provider it is: '"
						+ getRedirectUrl(request) + "'");
				throw new RedirectUriMismatch();
			} else {
				throw new AuthException(error);
			}
		} else if (isCallbackRequest(context)) {
			// second step in auth process
            final UUID storedState = this.auth.getFromCache(context.session(), STATE_TOKEN);
            if(storedState == null) {
                Logger.warn("Cache either timed out, or you are using a setup with multiple servers and a non-shared cache implementation");
                // we will just behave as if there was no auth, yet...
                return generateRedirectUrl(context, request);
            }
            final String callbackState = request.getQueryString(Constants.STATE);
            if(!storedState.equals(UUID.fromString(callbackState))) {
                // the return callback may have been forged
                throw new AuthException(messagesApi.preferred(request).at("playauthenticate.core.exception.oauth2.state_param_forged"));
            }
			final String code = request.getQueryString(Constants.CODE);
			final I info = getAccessToken(code, request);
            return transform(info, callbackState);
		} else {
			// no auth, yet
            return generateRedirectUrl(context, request);
		}
	}

    private String generateRedirectUrl(Context context, Request request) throws AuthException {
        final UUID state = UUID.randomUUID();
		this.auth.storeInCache(context.session(), STATE_TOKEN, state);
        final String url = getAuthUrl(request, state.toString());
        Logger.debug("generated redirect URL for dialog: " + url);
        return url;
    }

    protected boolean isCallbackRequest(final Context context) {
		return context.request().queryString().containsKey(Constants.CODE);
	}

	protected String getErrorParameterKey() {
		return Constants.ERROR;
	}

    @Override
    public void afterSave(final AuthUser user, final Object identity, final Session session) {
		this.auth.removeFromCache(session, STATE_TOKEN);
    }

	/**
	 * This allows custom implementations to enrich an AuthUser object or
	 * provide their own implementation
	 *
	 * @param info
	 * @param state
	 * @return
	 * @throws AuthException
	 */
	protected abstract AuthUserIdentity transform(final I info,
			final String state) throws AuthException;
}

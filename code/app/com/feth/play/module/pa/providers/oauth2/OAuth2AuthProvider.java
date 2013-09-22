package com.feth.play.module.pa.providers.oauth2;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import play.Application;
import play.Configuration;
import play.Logger;
import play.libs.WS;
import play.libs.WS.Response;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.exceptions.RedirectUriMismatch;
import com.feth.play.module.pa.providers.ext.ExternalAuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;

public abstract class OAuth2AuthProvider<U extends AuthUserIdentity, I extends OAuth2AuthInfo>
		extends ExternalAuthProvider {

	public OAuth2AuthProvider(final Application app) {
		super(app);
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

	protected String getAccessTokenParams(final Configuration c,
			final String code, Request request) {
		final List<NameValuePair> params = getParams(request, c);
		params.add(new BasicNameValuePair(Constants.CLIENT_SECRET, c
				.getString(SettingKeys.CLIENT_SECRET)));
		params.add(new BasicNameValuePair(Constants.GRANT_TYPE,
				Constants.AUTHORIZATION_CODE));
		params.add(new BasicNameValuePair(Constants.CODE, code));

		return URLEncodedUtils.format(params, "UTF-8");
	}

	protected I getAccessToken(final String code, final Request request)
			throws AccessTokenException {
		final Configuration c = getConfiguration();
		final String params = getAccessTokenParams(c, code, request);
		final String url = c.getString(SettingKeys.ACCESS_TOKEN_URL);
		final Response r = WS.url(url)
				.setHeader("Content-Type", "application/x-www-form-urlencoded")
				.post(params).get(PlayAuthenticate.TIMEOUT);

		return buildInfo(r);
	}

	protected abstract I buildInfo(final Response r)
			throws AccessTokenException;

	protected String getAuthUrl(final Request request, final String state)
			throws AuthException {
		final Configuration c = getConfiguration();
		final List<NameValuePair> params = getAuthParams(c, request, state);
		return generateURI(c.getString(SettingKeys.AUTHORIZATION_URL), params);
	}
	
	protected List<NameValuePair> getAuthParams(final Configuration c,
			final Request request, final String state) throws AuthException {
		final List<NameValuePair> params = getParams(request, c);
		if (c.getString(SettingKeys.SCOPE) != null) {
			params.add(new BasicNameValuePair(Constants.SCOPE, c
					.getString(SettingKeys.SCOPE)));
		}

		params.add(new BasicNameValuePair(Constants.RESPONSE_TYPE,
				Constants.CODE));

		if (c.getString(SettingKeys.ACCESS_TYPE) != null) {
			params.add(new BasicNameValuePair(Constants.ACCESS_TYPE, c
					.getString(SettingKeys.ACCESS_TYPE)));
		}

		if (c.getString(SettingKeys.APPROVAL_PROMPT) != null) {
			params.add(new BasicNameValuePair(Constants.APPROVAL_PROMPT, c
					.getString(SettingKeys.APPROVAL_PROMPT)));
		}

		if (state != null) {
			params.add(new BasicNameValuePair(Constants.STATE, state));
		}
		return params;
	}

	protected List<NameValuePair> getParams(final Request request,
			final Configuration c) {
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

		final String error = Authenticate.getQueryString(request,
				getErrorParameterKey());

		// Attention: facebook does *not* support state that is non-ASCII - not
		// even encoded.
		final String state = Authenticate.getQueryString(request,
				Constants.STATE);

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
			final String code = Authenticate
					.getQueryString(request, Constants.CODE);
			
			final I info = getAccessToken(code, request);
			final AuthUserIdentity u = transform(info, state);
			return u;
			// System.out.println(accessToken.getAccessToken());
		} else {
			// no auth, yet
			final String url = getAuthUrl(request, state);
			Logger.debug("generated redirect URL for dialog: " + url);
			return url;
		}
	}
	
	protected boolean isCallbackRequest(final Context context) {
		return context.request().queryString().containsKey(Constants.CODE);
	}

	protected String getErrorParameterKey() {
		return Constants.ERROR;
	}

	/**
	 * This allows custom implementations to enrich an AuthUser object or
	 * provide their own implementaion
	 * 
	 * @param info
	 * @param state
	 * @return
	 * @throws AuthException
	 */
	protected abstract AuthUserIdentity transform(final I info,
			final String state) throws AuthException;
}

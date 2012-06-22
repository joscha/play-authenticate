package com.feth.play.module.pa.providers.oauth2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
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
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.exceptions.RedirectUriMismatch;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.SessionAuthUser;

public abstract class OAuth2AuthProvider<U extends AuthUserIdentity, I extends OAuth2AuthInfo>
		extends AuthProvider {
	
	public OAuth2AuthProvider(final Application app) {
		super(app);
	}
	
	@Override
	protected List<String> neededSettingKeys() {
		return Arrays.asList(SettingKeys.ACCESS_TOKEN_URL, SettingKeys.AUTHORIZATION_URL, SettingKeys.CLIENT_ID, SettingKeys.CLIENT_SECRET);
	}

	public static abstract class SettingKeys {
		public static final String AUTHORIZATION_URL = "authorizationUrl";
		public static final String ACCESS_TOKEN_URL = "accessTokenUrl";
		public static final String CLIENT_ID = "clientId";
		public static final String CLIENT_SECRET = "clientSecret";
		public static final String SCOPE = "scope";
	}

	public static abstract class Constants {
		public static final String CLIENT_ID = "client_id";
		public static final String CLIENT_SECRET = "client_secret";
		public static final String REDIRECT_URI = "redirect_uri";
		public static final String SCOPE = "scope";
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

	private String getAccessTokenParams(final Configuration c,
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

	protected String getAuthUrl(final Request request, final String state) {

		final Configuration c = getConfiguration();
		final List<NameValuePair> params = getParams(request, c);
		params.add(new BasicNameValuePair(Constants.SCOPE, c
				.getString(SettingKeys.SCOPE)));
		params.add(new BasicNameValuePair(Constants.RESPONSE_TYPE,
				Constants.CODE));
		if (state != null) {
			params.add(new BasicNameValuePair(Constants.STATE, state));
		}

		final HttpGet m = new HttpGet(
				c.getString(SettingKeys.AUTHORIZATION_URL) + "?"
						+ URLEncodedUtils.format(params, "UTF-8"));

		return m.getURI().toString();
	}

	private List<NameValuePair> getParams(final Request request,
			final Configuration c) {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(Constants.CLIENT_ID, c
				.getString(SettingKeys.CLIENT_ID)));
		params.add(new BasicNameValuePair(Constants.REDIRECT_URI,
				getRedirectUrl(request)));
		return params;
	}

	private String getRedirectUrl(final Request request) {
		return PlayAuthenticate.getResolver().auth(getKey())
				.absoluteURL(request);
	}

	@Override
	public Object authenticate(final Context context, final Object payload) throws AuthException {

		final Request request = context.request();
		final String error = request.getQueryString(Constants.ERROR);
		final String code = request.getQueryString(Constants.CODE);

		if (error != null) {
			if (error.equals(Constants.ACCESS_DENIED)) {
				throw new AccessDeniedException();
			} else if(error.equals(Constants.REDIRECT_URI_MISMATCH)) {
				Logger.error("You must set the redirect URI for your provider to whatever you defined in your routes file." +
						"For this provider it is: '"+getRedirectUrl(request)+"'");
				throw new RedirectUriMismatch();
			} else {
				throw new AuthException(error);
			}
		} else if (code != null) {
			// second step in auth process
			final I info = getAccessToken(code, request);

			final AuthUserIdentity u = transform(info);
			return u;
			// System.out.println(accessToken.getAccessToken());
		} else {
			// no auth, yet
			final String url = getAuthUrl(request, null);
			Logger.debug("generated redirect URL for dialog: "+url);
			return url;
		}
	}
	
	@Override
	public AuthUser getSessionAuthUser(String id, long expires) {
		return new SessionAuthUser(getKey(), id, expires);
	}

	/**
	 * This allows custom implementations to enrich an AuthUser object or
	 * provide their own implementaion
	 * 
	 * @param info
	 * @return
	 * @throws AuthException
	 */
	protected abstract AuthUserIdentity transform(final I info)
			throws AuthException;
}

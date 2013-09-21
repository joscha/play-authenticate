package com.feth.play.module.pa.providers.oauth2.pocket;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import play.Application;
import play.Configuration;
import play.Logger;
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.Response;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.exceptions.RedirectUriMismatch;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;

public class PocketAuthProvider extends
    OAuth2AuthProvider<PocketAuthUser, PocketAuthInfo> {

	static final String PROVIDER_KEY = "pocket";
	String requestToken;

	public PocketAuthProvider(Application app) {
		super(app);
	}

	public static abstract class SettingKeys {
		public static final String AUTHORIZATION_URL = "authorizationUrl";
		public static final String ACCESS_TOKEN_URL = "accessTokenUrl";
		public static final String REQUEST_TOKEN_URL = "requestTokenUrl";
		public static final String CONSUMER_KEY = "consumer_key";
	}

	@Override
	protected List<String> neededSettingKeys() {
		final List<String> ret = new ArrayList<String>();
		ret.add(SettingKeys.ACCESS_TOKEN_URL);
		ret.add(SettingKeys.AUTHORIZATION_URL);
		ret.add(SettingKeys.REQUEST_TOKEN_URL);
		ret.add(SettingKeys.CONSUMER_KEY);
		return ret;
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	protected PocketAuthInfo buildInfo(Response r) throws AccessTokenException {
		String body = r.getBody();
		String accessToken = body.substring(body.indexOf("=") + 1, body.indexOf("&"));
		String userName = body.substring(body.indexOf("=", body.indexOf("=") + 1) + 1); 
		PocketAuthInfo info = new PocketAuthInfo(accessToken, requestToken, userName);
		return info;
	}

	@Override
	protected AuthUserIdentity transform(PocketAuthInfo info, String state)
	    throws AuthException {
		PocketAuthUser user = new PocketAuthUser(info, state);
		return user;
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
		final String code = Authenticate.getQueryString(request, Constants.CODE);

		final String state = Authenticate.getQueryString(request, Constants.STATE);

		if (error != null) {
			if (error.equals(Constants.ACCESS_DENIED)) {
				throw new AccessDeniedException(getKey());
			} else if (error.equals(Constants.REDIRECT_URI_MISMATCH)) {
				Logger.error("You must set the redirect URI for your provider to whatever you defined in your routes file."
				    + "For this provider it is: '" + getRedirectUrl(request) + "'");
				throw new RedirectUriMismatch();
			} else {
				throw new AuthException(error);
			}
		} else if (requestToken != null) {
			// second step in auth process
			final PocketAuthInfo info = getAccessToken(code, request);
			final AuthUserIdentity u = transform(info, state);
			return u;
		} else {
			// no auth, yet
			requestToken = getRequestToken(request);
			final String url = getAuthUrl(request, state);
			Logger.debug("generated redirect URL for dialog: " + url);
			return url;
		}
	}
	
	@Override
	protected PocketAuthInfo getAccessToken(final String code, final Request request)
			throws AccessTokenException {
		final Configuration c = getConfiguration();
		final String url = c.getString(SettingKeys.ACCESS_TOKEN_URL);
		final String params = getAccessTokenParams(c, code, request);
		final Promise<Response> r = WS.url(url)
		    .setHeader("Content-Type", "application/x-www-form-urlencoded")
		    .post(params);
		        
		final Response response = r.get();
		if (response.getStatus() > 400) {
			throw new AccessTokenException(response.asJson()
			    .get("meta")
			    .get("errorDetail")
			    .asText());
		} else {
			return buildInfo(response);
				
		}
	}
	
	protected String getAccessTokenParams(final Configuration c,
			final String code, Request request) {
		return Constants.CONSUMER_KEY + "="
        + c.getString(SettingKeys.CONSUMER_KEY) + "&"
        + Constants.CODE + "=" + requestToken;
	}

	protected String getRequestToken(final Request request) throws AuthException {
		final Configuration c = getConfiguration();
		final Promise<Response> r = WS.url(
		    c.getString(SettingKeys.REQUEST_TOKEN_URL))
		    .setHeader("Content-Type", "application/x-www-form-urlencoded")
		    .post(getRequestTokenParams(request, c));
		        
		final Response response = r.get();
		if (response.getStatus() > 400) {
			throw new AuthException(response.asJson()
			    .get("meta")
			    .get("errorDetail")
			    .asText());
		} else {
			String body = response.getBody();
			if(body != null && !body.equals("") && body.indexOf("=") != -1) {
				return body.substring(body.indexOf("=") + 1);
			}
			else {
				throw new AuthException("No token could be found in body response");
			}
		}
	}

	@Override
	protected String getAuthUrl(final Request request, final String state) {
		final Configuration c = getConfiguration();
		final List<NameValuePair> params = getAuthParams(request, c);
		final HttpPost m = new HttpPost(c.getString(SettingKeys.AUTHORIZATION_URL)
		    + "?" + URLEncodedUtils.format(params, "UTF-8"));
		return m.getURI()
		    .toString();
	}

	protected String getRequestTokenParams(final Request request,
	    final Configuration c) {
		return Constants.CONSUMER_KEY + "="
        + c.getString(SettingKeys.CONSUMER_KEY) + "&"
        + getRedirectUriKey() + "=" + getRedirectUrl(request);
	}

	protected List<NameValuePair> getAuthParams(final Request request,
	    final Configuration c) {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(Constants.REQUEST_TOKEN, requestToken));
		params.add(new BasicNameValuePair(getRedirectUriKey(),
		    getRedirectUrl(request)));
		return params;
	}
}

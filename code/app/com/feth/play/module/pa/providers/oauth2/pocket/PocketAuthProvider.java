package com.feth.play.module.pa.providers.oauth2.pocket;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AuthUserIdentity transform(PocketAuthInfo info, String state)
	    throws AuthException {
		// TODO Auto-generated method stub
		return null;
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

		// Attention: facebook does *not* support state that is non-ASCII - not
		// even encoded.
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
		} else if (code != null) {
			// second step in auth process
			final PocketAuthInfo info = getAccessToken(code, request);
			final AuthUserIdentity u = transform(info, state);
			return u;
			// System.out.println(accessToken.getAccessToken());
		} else {
			// no auth, yet
			requestToken = getRequestToken(request);
			final String url = getAuthUrl(request, state);
			Logger.debug("generated redirect URL for dialog: " + url);
			return url;
		}
	}

	protected String getRequestToken(final Request request) throws AuthException {
		final Configuration c = getConfiguration();
		final Promise<Response> r = WS.url(
		    c.getString(SettingKeys.REQUEST_TOKEN_URL))
		    .setHeader("Content-Type", "application/x-www-form-urlencoded")
		    .post(
		        Constants.CONSUMER_KEY + "="
		            + c.getString(SettingKeys.CONSUMER_KEY) + "&"
		            + getRedirectUriKey() + "=" + getRedirectUrl(request));
		final JsonNode response = r.get().asJson();
		System.out.println("PocketAuthProvider.getRequestToken() response: " + response + ", response.get(\"code\").asText(): " + response.get("code").asText());
		return response.get("code").asText();
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

	protected StringEntity getRequestTokenParams(final Request request,
	    final Configuration c) {
		StringBuilder sb = new StringBuilder();
		sb.append(new BasicNameValuePair(Constants.CONSUMER_KEY,
		    c.getString(SettingKeys.CONSUMER_KEY)));
		sb.append("&"
		    + new BasicNameValuePair(getRedirectUriKey(), getRedirectUrl(request)));
		try {
			return new StringEntity(sb.toString());
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected List<NameValuePair> getAuthParams(final Request request,
	    final Configuration c) {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(Constants.REQUEST_TOKEN, "\"" + requestToken + "\""));
		params.add(new BasicNameValuePair(getRedirectUriKey(),
		    getRedirectUrl(request)));
		return params;
	}
}

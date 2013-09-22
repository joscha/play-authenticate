package com.feth.play.module.pa.providers.oauth2.pocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

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
		public static final String REQUEST_TOKEN = "request_token";
	}
	
	public static abstract class PocketConstants extends Constants {
		public static final String CONSUMER_KEY = "consumer_key";
		public static final String REQUEST_TOKEN = "request_token";
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
		if (r.getStatus() >= 400) {
			throw new AccessTokenException(r.asJson()
			    .asText());
		} else {
			JsonNode response = r.asJson();
			String accessToken = response.get("access_token")
			    .asText();
			String userName = response.get("username")
			    .asText();
			PocketAuthInfo info = new PocketAuthInfo(accessToken, requestToken,
			    userName);
			return info;
		}
	}

	@Override
	protected AuthUserIdentity transform(PocketAuthInfo info, String state)
	    throws AuthException {
		return new PocketAuthUser(info, state);
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
	protected PocketAuthInfo getAccessToken(final String code,
	    final Request request) throws AccessTokenException {
		final Configuration c = getConfiguration();
		final List<NameValuePair> params = getAccessTokenParams(c, request);
		final String url = c.getString(SettingKeys.ACCESS_TOKEN_URL);
		final Response r = WS.url(url)
		    .setHeader("Content-Type", "application/json")
		    .setHeader("X-Accept", "application/json")
		    .post(encodeParamsAsJson(params))
		    .get(PlayAuthenticate.TIMEOUT);

		return buildInfo(r);
	}

	protected List<NameValuePair> getAccessTokenParams(final Configuration c,
	    Request request) {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PocketConstants.CONSUMER_KEY,
		    c.getString(SettingKeys.CONSUMER_KEY)));
		params.add(new BasicNameValuePair(Constants.CODE, requestToken));
		return params;
	}

	protected String getRequestToken(final Request request) throws AuthException {
		final Configuration c = getConfiguration();
		final List<NameValuePair> params = getRequestTokenParams(request, c);
		final Response r = WS.url(c.getString(SettingKeys.REQUEST_TOKEN_URL))
		    .setHeader("Content-Type", "application/json")
		    .setHeader("X-Accept", "application/json")
		    .post(encodeParamsAsJson(params))
		    .get(PlayAuthenticate.TIMEOUT);

		if (r.getStatus() >= 400) {
			throw new AuthException(r.asJson().asText());
		} else {
			return r.asJson().get("code").asText();
		}
	}

	protected List<NameValuePair> getRequestTokenParams(final Request request,
	    final Configuration c) {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PocketConstants.CONSUMER_KEY,
		    c.getString(SettingKeys.CONSUMER_KEY)));
		params.add(new BasicNameValuePair(getRedirectUriKey(),
		    getRedirectUrl(request)));
		return params;
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

	protected List<NameValuePair> getAuthParams(final Request request,
	    final Configuration c) {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PocketConstants.REQUEST_TOKEN, requestToken));
		params.add(new BasicNameValuePair(getRedirectUriKey(),
		    getRedirectUrl(request)));
		return params;
	}

	private JsonNode encodeParamsAsJson(List<NameValuePair> params) {
		Map<String, String> map = new HashMap<String, String>();
		for (NameValuePair nameValuePair : params) {
			map.put(nameValuePair.getName(), nameValuePair.getValue());
		}
		return new ObjectMapper().valueToTree(map);
	}

}

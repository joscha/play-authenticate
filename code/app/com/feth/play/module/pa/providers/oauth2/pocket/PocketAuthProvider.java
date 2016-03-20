package com.feth.play.module.pa.providers.oauth2.pocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.exceptions.ResolverMissingException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import play.Configuration;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http.Request;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Singleton
public class PocketAuthProvider extends
		OAuth2AuthProvider<PocketAuthUser, PocketAuthInfo> {

	public static final String PROVIDER_KEY = "pocket";

	@Inject
	public PocketAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final WSClient wsClient) {
		super(auth, lifecycle, wsClient);
	}

	public static abstract class SettingKeys extends
			OAuth2AuthProvider.SettingKeys {
		public static final String REQUEST_TOKEN_URL = "requestTokenUrl";
		public static final String CONSUMER_KEY = "consumerKey";
	}

	public static abstract class PocketConstants extends Constants {
		public static final String CONSUMER_KEY = "consumer_key";
		public static final String REQUEST_TOKEN = "request_token";
	}

	private static JsonNode encodeParamsAsJson(final List<NameValuePair> params) {
		final Map<String, String> map = new HashMap<String, String>(params.size());
		for (final NameValuePair nameValuePair : params) {
			map.put(nameValuePair.getName(), nameValuePair.getValue());
		}
		return new ObjectMapper().valueToTree(map);
	}

	@Override
	protected List<String> neededSettingKeys() {
		return Arrays.asList(SettingKeys.ACCESS_TOKEN_URL,
				SettingKeys.AUTHORIZATION_URL, SettingKeys.REQUEST_TOKEN_URL,
				SettingKeys.CONSUMER_KEY);
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	protected PocketAuthInfo buildInfo(final WSResponse r)
			throws AccessTokenException {
		if (r.getStatus() >= 400) {
			throw new AccessTokenException(r.asJson().asText());
		} else {
			final List<NameValuePair> list = URLEncodedUtils.parse(r.getBody(),
					Charset.forName("UTF-8"));
			final Map<String, String> map = new HashMap<String, String>(
					list.size());
			for (final NameValuePair nameValuePair : list) {
				map.put(nameValuePair.getName(), nameValuePair.getValue());
			}
			return new PocketAuthInfo(map);
		}
	}

	@Override
	protected AuthUserIdentity transform(final PocketAuthInfo info,
			final String state) throws AuthException {
		return new PocketAuthUser(info, state);
	}

	@Override
	protected String getAccessTokenParams(final Configuration c,
			final String code, final Request request) {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PocketConstants.CONSUMER_KEY, c
				.getString(SettingKeys.CONSUMER_KEY)));
		params.add(new BasicNameValuePair(Constants.CODE, code));

		return URLEncodedUtils.format(params, "UTF-8");
	}

	private String getRequestToken(final Request request) throws AuthException {
		final Configuration c = getConfiguration();
		final List<NameValuePair> params = getRequestTokenParams(request, c);

		try {
			final WSResponse r = wsClient.url(c.getString(SettingKeys.REQUEST_TOKEN_URL))
					.setHeader("Content-Type", "application/json")
					.setHeader("X-Accept", "application/json")
					.post(encodeParamsAsJson(params)).toCompletableFuture().get(getTimeout(), TimeUnit.MILLISECONDS);

			if (r.getStatus() >= 400) {
				throw new AuthException(r.asJson().asText());
			} else {
				return r.asJson().get(PocketConstants.CODE).asText();
			}
		} catch(InterruptedException | ExecutionException | TimeoutException e) {
			throw new AuthException(e.getMessage(), e);
		}
	}

	private List<NameValuePair> getRequestTokenParams(final Request request,
			final Configuration c) throws ResolverMissingException {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PocketConstants.CONSUMER_KEY, c
				.getString(SettingKeys.CONSUMER_KEY)));
		params.add(new BasicNameValuePair(getRedirectUriKey(),
				getRedirectUrl(request)));
		return params;
	}

	@Override
	protected List<NameValuePair> getAuthParams(final Configuration c,
			final Request request, final String state) throws AuthException {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PocketConstants.CONSUMER_KEY, c
				.getString(SettingKeys.CONSUMER_KEY)));

		final String requestToken = this.getRequestToken(request);
		params.add(new BasicNameValuePair(PocketConstants.REQUEST_TOKEN,
				requestToken));

		if (state != null) {
			params.add(new BasicNameValuePair(PocketConstants.STATE, state));
		}

		// with this we fake the response to contain the response to Play!
		// Authenticate to contain the request token in the "code" parameter
		final String redirectUrl = getRedirectUrl(request,
				Arrays.asList(new BasicNameValuePair(PocketConstants.CODE,
						requestToken)));

		params.add(new BasicNameValuePair(getRedirectUriKey(), redirectUrl));
		return params;
	}
}

package com.feth.play.module.pa.providers.oauth2.untappd;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.exceptions.ResolverMissingException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.google.inject.Inject;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http.Request;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Auth provider for Untappd beer social network
 * https://www.untappd.com
 */
@Singleton
public class UntappdAuthProvider extends
		OAuth2AuthProvider<UntappdAuthUser, UntappdAuthInfo> {

	public static final String PROVIDER_KEY = "untappd";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";

	private static final String NODE_USER = "user";
	private static final String NODE_RESPONSE = "response";
	private static final String NODE_META = "meta";

	private static final String ERROR_DETAIL = "error_detail";
	private static final String ERROR_TYPE = "error_type";

	private static final String REDIRECT_URL = "redirect_url";

	// Use this value for REDIRECT_URL in local development
	// and put same URL in your Untappd App page
	// private static final String CALLBACK_URL =
	// "http://localhost:9000/authenticate/untappd";

	@Inject
	public UntappdAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final WSClient wsClient) {
		super(auth, lifecycle, wsClient);
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	protected UntappdAuthUser transform(final UntappdAuthInfo info,
			final String state) throws AuthException {

		final String url = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);

		final WSResponse r = fetchAuthResponse(url,
				new QueryParam(OAuth2AuthProvider.Constants.ACCESS_TOKEN, info.getAccessToken())
		);
		final JsonNode result = r.asJson();
		if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
			throw new AuthException(result.get(
					OAuth2AuthProvider.Constants.ERROR).asText());
		} else {
			Logger.debug(result.toString());
			return new UntappdAuthUser(
					result.get(NODE_RESPONSE).get(NODE_USER), info, state);
		}
	}

	@Override
	protected String getErrorParameterKey() {
		return ERROR_TYPE;
	}

	@Override
	protected String getRedirectUriKey() {
		// Attention: This is redirect_urL instead of the normal redirect_urI
		return REDIRECT_URL;
	}

	protected UntappdAuthInfo getAccessToken(final String code,
			final Request request) throws AccessTokenException, ResolverMissingException {
		final Configuration c = getConfiguration();

		final String url = c.getString(SettingKeys.ACCESS_TOKEN_URL);

		try {
			final WSResponse r = wsClient
					.url(url)
					.setQueryParameter(Constants.CLIENT_ID,
							c.getString(SettingKeys.CLIENT_ID))
					.setQueryParameter(Constants.CLIENT_SECRET,
							c.getString(SettingKeys.CLIENT_SECRET))
					.setQueryParameter(Constants.RESPONSE_TYPE, Constants.CODE)
					.setQueryParameter(Constants.CODE, code)
					.setQueryParameter(getRedirectUriKey(), getRedirectUrl(request))
					// we use GET here
					.get().toCompletableFuture().get(getTimeout(), TimeUnit.MILLISECONDS);

			return buildInfo(r);
		} catch(InterruptedException | ExecutionException | TimeoutException e) {
			throw new AccessTokenException(e.getMessage(), e);
		}
	}

	@Override
	protected List<NameValuePair> getParams(final Request request,
			final Configuration c) throws ResolverMissingException {
		final List<NameValuePair> params = super.getParams(request, c);

		params.add(new BasicNameValuePair(Constants.CLIENT_SECRET, c
				.getString(SettingKeys.CLIENT_SECRET)));
		return params;
	}

	@Override
	protected UntappdAuthInfo buildInfo(final WSResponse r)
			throws AccessTokenException {
		final JsonNode n = r.asJson();

		final JsonNode meta = n.get(NODE_META);
		if (meta.get(ERROR_TYPE) != null) {
			throw new AccessTokenException(meta.get(ERROR_DETAIL).asText());
		} else {
			return new UntappdAuthInfo(n.get(NODE_RESPONSE)
					.get(Constants.ACCESS_TOKEN).asText());
		}
	}

}

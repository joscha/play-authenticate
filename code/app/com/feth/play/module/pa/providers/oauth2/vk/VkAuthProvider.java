package com.feth.play.module.pa.providers.oauth2.vk;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Denis Borisenko
 */
@Singleton
public class VkAuthProvider extends OAuth2AuthProvider<VkAuthUser, VkAuthInfo> {

	public static final String PROVIDER_KEY = "vk";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
	private static final String USER_INFO_FIELDS_SETTING_KEY = "userInfoFields";

	private static final String FIELDS_REQUEST_KEY = "fields";
	private static final String UIDS_REQUEST_KEY = "uids";

	private static final String BODY_RESPONSE_KEY = "response";

	@Inject
	public VkAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final WSClient wsClient) {
		super(auth, lifecycle, wsClient);
	}

	@Override
	protected VkAuthUser transform(VkAuthInfo info, final String state)
			throws AuthException {

		final String url = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);
		final String fields = getConfiguration().getString(
				USER_INFO_FIELDS_SETTING_KEY);
		final WSResponse r = fetchAuthResponse(url,
				new QueryParam(UIDS_REQUEST_KEY, info.getUserId()),
				new QueryParam(FIELDS_REQUEST_KEY, fields)
		);

		final JsonNode result = r.asJson();

		if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
			throw new AuthException(result.get(
					OAuth2AuthProvider.Constants.ERROR).asText());
		} else {
			return new VkAuthUser(result.get(BODY_RESPONSE_KEY).get(0), info,
					state);
		}
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	protected VkAuthInfo buildInfo(final WSResponse r)
			throws AccessTokenException {
		final JsonNode n = r.asJson();
		Logger.debug(n.toString());

		if (n.get(OAuth2AuthProvider.Constants.ERROR) != null) {
			throw new AccessTokenException(n.get(
					OAuth2AuthProvider.Constants.ERROR).asText());
		} else {
			return new VkAuthInfo(n);
		}
	}
}

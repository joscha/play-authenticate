package com.feth.play.module.pa.providers.oauth2.vk;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;

/**
 * @author Denis Borisenko
 */
public class VkAuthInfo extends OAuth2AuthInfo {

	private static final long serialVersionUID = 1L;

	private String userId;

	private static final String USER_ID = "user_id";

	public VkAuthInfo(final JsonNode node) {
		super(	node.get(OAuth2AuthProvider.Constants.ACCESS_TOKEN).asText(),
				new Date().getTime() + node.get(OAuth2AuthProvider.Constants.EXPIRES_IN).asLong() * 1000);

		if (node.has(USER_ID)) {
			this.userId = node.get(USER_ID).asText();
		}
	}

	public String getUserId() {
		return userId;
	}
}

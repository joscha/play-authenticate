package com.feth.play.module.pa.providers.oauth2.google;

import java.util.Date;

import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.Constants;

public class GoogleAuthInfo extends OAuth2AuthInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String ID_TOKEN = "id_token";
	private String bearer;
	private String idToken;

	public GoogleAuthInfo(final JsonNode node) {
		super(node.get(Constants.ACCESS_TOKEN).asText(), new Date().getTime()
				+ node.get(Constants.EXPIRES_IN).asLong() * 1000);
		bearer = node.get(Constants.TOKEN_TYPE).asText();
		idToken = node.get(ID_TOKEN).asText();
	}

	public String getBearer() {
		return bearer;
	}

	public String getIdToken() {
		return idToken;
	}
}

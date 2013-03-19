package com.feth.play.module.pa.providers.oauth2.google;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.Constants;
import org.codehaus.jackson.JsonNode;

import java.util.Date;

public class GoogleAuthInfo extends OAuth2AuthInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	private static final String ID_TOKEN = "id_token";
	private String bearer;
	private String idToken;
    private String refreshToken;
    private long expiresIn;

	public GoogleAuthInfo(final JsonNode node) {
		super(node.get(Constants.ACCESS_TOKEN).asText(), new Date().getTime()
				+ node.get(Constants.EXPIRES_IN).asLong() * 1000);
		bearer = node.get(Constants.TOKEN_TYPE).asText();
		idToken = node.get(ID_TOKEN).asText();
        refreshToken = node.get(Constants.REFRESH_TOKEN).asText();
        expiresIn = node.get(Constants.EXPIRES_IN).asLong();
	}

	public String getBearer() {
		return bearer;
	}

	public String getIdToken() {
		return idToken;
	}

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}

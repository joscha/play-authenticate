package com.feth.play.module.pa.providers.oauth2.pocket;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;

public class PocketAuthInfo extends OAuth2AuthInfo {

	private static final long serialVersionUID = 1L;
	private String token;

	public PocketAuthInfo(final String accessToken, final String token) {
		super(accessToken);
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}

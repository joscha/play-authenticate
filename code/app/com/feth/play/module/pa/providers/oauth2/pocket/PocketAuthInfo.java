package com.feth.play.module.pa.providers.oauth2.pocket;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;

public class PocketAuthInfo extends OAuth2AuthInfo {

	private static final long serialVersionUID = 1L;
	private String requestToken;

	public PocketAuthInfo(final String accessToken, final String requestToken) {
		super(accessToken);
		this.requestToken = requestToken;
	}

	public String getRequestToken() {
		return requestToken;
	}

	public void setRequestToken(String requestToken) {
		this.requestToken = requestToken;
	}

}

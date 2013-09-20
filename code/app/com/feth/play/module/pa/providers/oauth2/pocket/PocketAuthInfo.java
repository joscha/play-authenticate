package com.feth.play.module.pa.providers.oauth2.pocket;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;

public class PocketAuthInfo extends OAuth2AuthInfo {

	private static final long serialVersionUID = 1L;
	private String requestToken;
	private String userName;

	public PocketAuthInfo(final String accessToken, final String requestToken,
	    final String userName) {
		super(accessToken);
		this.requestToken = requestToken;
		this.userName = userName;
	}

	public String getRequestToken() {
		return requestToken;
	}

	public String getUserName() {
		return userName;
	}

	public void setRequestToken(String requestToken) {
		this.requestToken = requestToken;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}

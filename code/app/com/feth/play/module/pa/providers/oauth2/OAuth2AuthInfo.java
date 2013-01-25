package com.feth.play.module.pa.providers.oauth2;

import com.feth.play.module.pa.providers.AuthInfo;
import com.feth.play.module.pa.user.AuthUser;

public abstract class OAuth2AuthInfo extends AuthInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String accessToken;
	private long expires;

	public OAuth2AuthInfo(final String token, final long l) {
		accessToken = token;
		expires = l;
	}

	public OAuth2AuthInfo(final String token) {
		this(token, AuthUser.NO_EXPIRATION);
	}

	public String getAccessToken() {
		return accessToken;
	}

	public long getExpiration() {
		return expires;
	}
}

package com.feth.play.module.pa.providers.oauth2;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.TokenIdentity;

public abstract class OAuth2AuthUser extends AuthUser implements TokenIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private OAuth2AuthInfo info;
	private String id;

	private final String state;

	public OAuth2AuthUser(final String id, final OAuth2AuthInfo info,
			final String state) {
		this.info = info;
		this.id = id;
		this.state = state;
	}

	public OAuth2AuthInfo getOAuth2AuthInfo() {
		return info;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long expires() {
		return getOAuth2AuthInfo().getExpiration();
	}

	public String getState() {
		return state;
	}

	@Override
	public String getToken() {
		return getOAuth2AuthInfo().getAccessToken();
	}
}

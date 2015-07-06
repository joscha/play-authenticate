package com.feth.play.module.pa.providers.oauth2.pocket;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthUser;

public class PocketAuthUser extends OAuth2AuthUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String username;

	public PocketAuthUser(final PocketAuthInfo info, final String state) {
		super(info.getUserName(), info, state);
		this.username = info.getUserName();
	}

	@Override
	public String getProvider() {
		return PocketAuthProvider.PROVIDER_KEY;
	}

	public String getUsername() {
		return username;
	}
}

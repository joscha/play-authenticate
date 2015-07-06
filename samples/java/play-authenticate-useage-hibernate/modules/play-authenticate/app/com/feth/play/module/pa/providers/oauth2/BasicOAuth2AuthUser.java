package com.feth.play.module.pa.providers.oauth2;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.BasicIdentity;

public abstract class BasicOAuth2AuthUser extends OAuth2AuthUser implements
		BasicIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BasicOAuth2AuthUser(final String id, final OAuth2AuthInfo info,
			final String state) {
		super(id, info, state);
	}

	@Override
	public String toString() {
		return AuthUser.toString(this);
	}

}

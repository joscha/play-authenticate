package com.feth.play.module.pa.providers.oauth2;

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
		final StringBuilder sb = new StringBuilder();
		if (getName() != null) {
			sb.append(getName());
			sb.append(" ");
		}
		if (getEmail() != null) {
			sb.append("(");
			sb.append(getEmail());
			sb.append(") ");
		}
		if (getEmail() != null || getName() != null) {
			sb.append("@ ");
		}
		sb.append(getProvider());

		return sb.toString();
	}

}

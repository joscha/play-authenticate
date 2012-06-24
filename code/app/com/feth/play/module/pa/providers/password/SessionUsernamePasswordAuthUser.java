package com.feth.play.module.pa.providers.password;

public class SessionUsernamePasswordAuthUser extends
		DefaultUsernamePasswordAuthUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final long expires;

	public SessionUsernamePasswordAuthUser(final String clearPassword,
			final String email, final long expires) {
		super(clearPassword, email);
		this.expires = expires;
	}

	@Override
	public long expires() {
		return expires;
	}
}

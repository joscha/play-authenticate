package com.feth.play.module.pa.user;

public class SessionAuthUser extends AuthUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final long expires;
	private final String provider;
	private final String id;
	
	public SessionAuthUser(final String provider, final String id, final long expires) {
		this.expires = expires;
		this.provider = provider;
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getProvider() {
		return provider;
	}

	@Override
	public long expires() {
		return expires;
	}
}

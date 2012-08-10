package com.feth.play.module.pa.user;

public class SessionAuthUser extends AuthUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final long expires;
	private final String provider;
	private final String id;
	private final String token;
	private final String tokenSecret;
	
	
	public SessionAuthUser(final String provider, final String id, final long expires, final String token, final String tokenSecret) {
		this.expires = expires;
		this.provider = provider;
		this.id = id;
		this.token = token;
		this.tokenSecret = tokenSecret;
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
	
	public String accessToken() {
		return token;
	}

	
	public String accessTokenSecret() {
		return tokenSecret;
	}
}

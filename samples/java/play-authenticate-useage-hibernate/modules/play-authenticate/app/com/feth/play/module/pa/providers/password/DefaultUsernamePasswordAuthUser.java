package com.feth.play.module.pa.providers.password;

public class DefaultUsernamePasswordAuthUser extends UsernamePasswordAuthUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DefaultUsernamePasswordAuthUser(final String clearPassword, final String email) {
		super(clearPassword, email);
	}
	
	@Override
	public String getId() {
		return super.getEmail();
	}

	/**
	 * This MUST be overwritten by an extending class.
	 * The default implementation stores a clear string, which is NOT recommended.
	 * 
	 * Should return null if the clearString given is null.
	 * 
	 * @return
	 */
	@Override
	protected String createPassword(final String clearString) {
		return clearString;
	}
}

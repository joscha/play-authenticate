package com.feth.play.module.pa.providers.password;

import org.mindrot.jbcrypt.BCrypt;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.EmailIdentity;

public abstract class UsernamePasswordAuthUser extends AuthUser implements EmailIdentity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final transient String password;
	private final String email;

	public UsernamePasswordAuthUser(final String clearPassword, final String email) {
		this.password = clearPassword;
		this.email = email;
	}
	
	/**
	 * Should return null if the clearString given is null.
	 * 
	 * @return
	 */
	//protected abstract String createPassword(final String clearString);
	
	/**
	 * Should return false if either the candidate or stored password is null.
	 * 
	 * @param candidate
	 * @return
	 */
	//public abstract boolean checkPassword(final String candidate);

	@Override
	public String getId() {
		return getHashedPassword();
	}

	@Override
	public String getProvider() {
		return UsernamePasswordAuthProvider.PROVIDER_KEY;
	}

	@Override
	public String getEmail() {
		return email;
	}

	public String getHashedPassword() {
		return createPassword(this.password);
	}
	
	/**
	 * You *SHOULD* provide your own implementation of this which implements your own security.
	 */
	protected String createPassword(final String clearString) {
		return BCrypt.hashpw(clearString, BCrypt.gensalt());
	}
	
	/**
	 * You *SHOULD* provide your own implementation of this which implements your own security.
	 */
	public boolean checkPassword(final String hashed, final String candidate) {
		if(hashed == null || candidate == null) {
			return false;
		}
		return BCrypt.checkpw(candidate, hashed);
	}
	
	public String getPassword() {
		return this.password;
	}
}

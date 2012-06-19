package com.feth.play.module.pa.service;

import com.feth.play.module.pa.providers.AuthUser;
import com.feth.play.module.pa.providers.AuthUserIdentity;

public interface UserService {
	
	/**
	 * Saves auth provider/id combination to a local user
	 * @param authUser
	 * @return The local identifying object or null if the user existed
	 */
	public Object save(final AuthUser authUser);

	/**
	 * Returns the local identifying object if the auth provider/id combination has been linked to a local user account already
	 * or null if not
	 * 
	 * @param identity
	 * @return
	 */
	public Object getLocalIdentity(final AuthUserIdentity identity);

	/**
	 * Merges two user accounts after a login with an auth provider/id that is linked to a different account than the login from before
	 * Returns the user to generate the session information from
	 * 
	 * @param newUser
	 * @param oldUser
	 * @return
	 */
	public AuthUser merge(final AuthUser newUser, final AuthUser oldUser);

	/**
	 * Links a new account to an exsting local user.
	 * Returns the auth user to log in with
	 * 
	 * @param oldUser
	 * @param newUser
	 */
	public AuthUser link(final AuthUser oldUser, final AuthUser newUser);
}

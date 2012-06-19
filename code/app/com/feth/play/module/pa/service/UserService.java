package com.feth.play.module.pa.service;

import com.feth.play.module.pa.providers.AuthUser;
import com.feth.play.module.pa.providers.AuthUserIdentity;

public interface UserService {

	/**
	 * generates a user identity from an auth provider/id combination
	 * 
	 * @param provider
	 * @param id
	 * @return
	 */
	public AuthUserIdentity find(final String provider, final String id);
	
	/**
	 * Saves auth provider/id combination to a local user
	 * @param authUser
	 * @return
	 */
	public Object save(final AuthUser authUser);

	/**
	 * Returns true if the auth provider/id combination has been linked to a local user account already
	 * @param identity
	 * @return
	 */
	public boolean isLinked(final AuthUserIdentity identity);

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

package com.feth.play.module.pa.service;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;

public interface UserService {
	
	/**
	 * Saves auth provider/id combination to a local user
	 * @param authUser
	 * @return The local identifying object or null if the user existed
	 */
	public Object save(final AuthUser authUser);

	/**
	 * Returns the local identifying object if the auth provider/id combination has been linked to a local user account already
	 * or null if not.
	 * This gets called on any login to check whether the session user still has a valid corresponding local user
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

	/**
	 * Gets called when a user logs in - you might make profile updates here with data coming from the login provider
	 * or bump a last-logged-in date
	 * 
	 * @param knownUser
	 * @return
	 */
	public AuthUser update(final AuthUser knownUser);
}

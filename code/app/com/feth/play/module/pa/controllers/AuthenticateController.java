package com.feth.play.module.pa.controllers;

import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.user.AuthUser;

public class AuthenticateController extends Controller {

	public static Result authenticate(final String provider) {
		final AuthProvider ap = AuthProvider.Registry.get(provider);
		if (ap == null) {
			// Provider wasn't found and/or user was fooling with our stuff
			return notFound();
		}
		try {
			final Object o = ap.authenticate(ctx());
			if (o instanceof String) {
				return redirect((String) o);
			} else if (o instanceof AuthUser) {

				final AuthUser u = (AuthUser) o;

				// We might want to do merging here:
				// http://stackoverflow.com/questions/6666267/architecture-for-merging-multiple-user-accounts-together
				// 1. The account is linked to a local account and no session
				// cookie is present --> Login
				// 2. The account is linked to a local account and a session
				// cookie is present --> Merge
				// 3. The account is not linked to a local account and no
				// session cookie is present --> Signup
				// 4. The account is not linked to a local account and a session
				// cookie is present --> Linking Additional account

				final boolean isLoggedIn = PlayAuthenticate
						.isLoggedIn(session());
				final Object loginIdentity = PlayAuthenticate.getUserService()
						.getLocalIdentity(u);
				final boolean isLinked = loginIdentity != null;

				// get the user with which we are logged in - is null if we are
				// not logged in
				final AuthUser oldUser = PlayAuthenticate.getUser(ctx());
				final AuthUser loginUser;
				if (isLinked && !isLoggedIn) {
					// 1. -> Login
					loginUser = u;
				} else if (isLinked && isLoggedIn) {
					// 2. -> Merge
					// merge the two identities and return the AuthUser we want
					// to use for the log in
					if (PlayAuthenticate.isAccountMergeEnabled()
							&& !loginIdentity
									.equals(PlayAuthenticate.getUserService()
											.getLocalIdentity(oldUser))) {
						// account merge is enabled
						// and
						// The currently logged in user and the one to log in
						// are not the same, so shall we merge?

						if (PlayAuthenticate.isAccountAutoMerge()) {
							loginUser = PlayAuthenticate.getUserService()
									.merge(u, oldUser);
						} else {
							final Call c = PlayAuthenticate.getResolver()
									.askMerge();
							if (c == null) {
								throw new RuntimeException(
										"Merge controller not defined, even though accountAutoMerge is set to false");
							}
							PlayAuthenticate.storeMergeUser(u, session());
							return redirect(c);
						}
					} else {
						// the currently logged in user and the new login belong
						// to the same local user,
						// or Account merge is disabled, so just change the log
						// in to the new user
						loginUser = u;
					}

				} else if (!isLinked && !isLoggedIn) {
					// 3. -> Signup
					loginUser = PlayAuthenticate.signupUser(u);
				} else {
					// !isLinked && isLoggedIn:
					// 4. -> Link additional
					if (PlayAuthenticate.isAccountAutoLink()) {
						loginUser = PlayAuthenticate.getUserService().link(
								oldUser, u);
					} else {
						final Call c = PlayAuthenticate.getResolver().askLink();
						if (c == null) {
							throw new RuntimeException(
									"Link controller not defined, even though accountAutoLink is set to false");
						}
						PlayAuthenticate.storeLinkUser(u, session());
						return redirect(c);
					}

				}

				return PlayAuthenticate.loginAndRedirect(ctx(), loginUser);
			} else {
				return internalServerError("Something went wrong");
			}
		} catch (final AuthException e) {
			if (e instanceof AccessTokenException) {
				return internalServerError("Exchanging request token for access token failed");
			} else if (e instanceof AccessDeniedException) {
				return forbidden(e.getMessage());
			}
			return internalServerError(e.getMessage());
		}
	}
}

package com.feth.play.module.pa.controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.providers.AuthUser;
import com.feth.play.module.pa.providers.AuthUserIdentity;

import play.Logger;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Result;

public class AuthenticateController extends Controller {

	private static String getJumpUrl(final Context ctx) {
		final String originalUrl = PlayAuthenticate.getOriginalUrl(ctx);
		if(originalUrl != null) {
			return originalUrl;
		} else {
			// this can be null if the user did not correctly define the resolver
			final Call c = PlayAuthenticate.getResolver().afterAuth();
			if(c != null) {
				return c.url();
			} else {
				// go to root instead, but log this
				Logger.warn("Resolver did not contain information about where to go after authentication - redirecting to /");
				final String afterAuthFallback = PlayAuthenticate.getConfiguration().getString("afterAuthFallback");
				if(afterAuthFallback != null && !afterAuthFallback.equals("")) {
					return afterAuthFallback;
				}
				// Not even the config setting was there or valid...meh
				return "/";
			}
		}
	}
	public static Result authenticate(final String provider) {
		final AuthProvider ap = AuthProvider.Registry.get(provider);
		if(ap == null) {
			// Provider wasn't found and/or user was fooling with our stuff
			return notFound();
		}
		try {
			final Object o = ap.authenticate(ctx());
			if(o instanceof String) {
				return redirect((String) o);
			} else if (o instanceof AuthUser) {
				
				final AuthUser u = (AuthUser) o;
				
				// We might want to do merging here:
				// http://stackoverflow.com/questions/6666267/architecture-for-merging-multiple-user-accounts-together
//				1. The account is linked to a local account and no session cookie is present --> Login
//				2. The account is linked to a local account and a session cookie is present --> Merge
//				3. The account is not linked to a local account and no session cookie is present --> Signup
//				4. The account is not linked to a local account and a session cookie is present --> Linking Additional account
				
				final boolean isLoggedIn = PlayAuthenticate.isLoggedIn(session());
				final boolean isLinked = PlayAuthenticate.getUserService().isLinked(u);
				
				// TODO allow deactivation of account merge
				final boolean accountMergeEnabled = PlayAuthenticate.isSuggestAccountMerge();
				// get the user with which we are logged in - is null if we are not logged in
				final AuthUser oldUser = PlayAuthenticate.getUser(ctx());
				final AuthUser loginUser;
				if(isLinked && !isLoggedIn) {
					// 1. -> Login
					loginUser = u;
				} else if(isLinked && isLoggedIn) {
					// 2. -> Merge
					// merge the two identities and return the AuthUser we want to use for the log in
					loginUser = PlayAuthenticate.getUserService().merge(u,oldUser);
					
				} else if(!isLinked && !isLoggedIn) {
					// 3. -> Signup
					final Long id = PlayAuthenticate.getUserService().save(u);
					if(id == null) {
						throw new AuthException("Could not sign you up");
					}
					loginUser = u;
				} else {
					// !isLinked && isLoggedIn:
					// 4. -> Link additional
					loginUser = PlayAuthenticate.getUserService().link(oldUser, u);
				}
				
				// log in the user
				PlayAuthenticate.storeUser(session(), loginUser);
				return redirect(getJumpUrl(ctx()));
			} else {
				return internalServerError("Something went wrong");
			}
		} catch (final AuthException e) {
			if(e instanceof AccessTokenException) {
				return internalServerError("Exchanging request token for access token failed");
			} else if(e instanceof AccessDeniedException) {
				return forbidden(e.getMessage());
			}
			return internalServerError(e.getMessage());
		}
	}
}

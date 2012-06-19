package com.feth.play.module.pa;

import java.util.Date;

import play.Configuration;
import play.Logger;
import play.Play;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Http.Session;
import play.mvc.Result;

import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.service.UserService;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.SessionAuthUser;

public abstract class PlayAuthenticate {

	public abstract static class Resolver {

		/**
		 * This is the route to your login page
		 * 
		 * @return
		 */
		public abstract Call login();

		/**
		 * Route to redirect to after authentication has been finished.
		 * Only used if no original URL was stored.
		 * If you return null here, the user will get redirected to the URL of
		 * the setting
		 * afterAuthFallback
		 * You can use this to redirect to an external URL for example.
		 * 
		 * @return
		 */
		public abstract Call afterAuth();

		/**
		 * This should usually point to the route where you registered
		 * com.feth.play.module.pa.controllers.AuthenticateController.
		 * authenticate(String)
		 * however you might provide your own authentication implementation if
		 * you want to
		 * and point it there
		 * 
		 * @param provider
		 *            The provider ID matching one of your registered providers
		 *            in play.plugins
		 * 
		 * @return a Call to follow
		 */
		public abstract Call auth(final String provider);

		/**
		 * If you set the accountAutoMerge setting to true, you might return
		 * null for this.
		 * 
		 * @return
		 */
		public abstract Call askMerge();

		/**
		 * If you set the accountAutoLink setting to true, you might return null
		 * for this
		 * 
		 * @return
		 */
		public abstract Call askLink();
	}

	private static Resolver resolver;

	public static void setResolver(Resolver res) {
		resolver = res;
	}

	public static Resolver getResolver() {
		return resolver;
	}

	private static UserService userService;

	public static void setUserService(final UserService service) {
		userService = service;
	}

	public static UserService getUserService() {
		if (userService == null) {
			throw new RuntimeException("No UserService registered!");
		}
		return userService;
	}

	private static final String ORIGINAL_URL = "pa.url.orig";
	private static final String USER_KEY = "pa.u.id";
	private static final String PROVIDER_KEY = "pa.p.id";
	private static final String EXPIRES_KEY = "pa.u.exp";
	private static final String SESSION_ID_KEY = "pa.s.id";

	public static Configuration getConfiguration() {
		return Play.application().configuration()
				.getConfig("play-authenticate");
	}

	public static final Long TIMEOUT = 10l * 1000;
	private static final String MERGE_USER_KEY = null;
	private static final String LINK_USER_KEY = null;

	public static String getOriginalUrl(final Http.Context context) {
		return context.session().remove(PlayAuthenticate.ORIGINAL_URL);
	}

	public static String storeOriginalUrl(final Http.Context context) {
		String loginUrl = null;
		if (PlayAuthenticate.getResolver().login() != null) {
			loginUrl = PlayAuthenticate.getResolver().login().url();
		} else {
			Logger.warn("You should define a login call in the resolver");
		}

		if (context.request().method().equals("GET")
				&& !context.request().path().equals(loginUrl)) {
			Logger.debug("Path where we are coming from ("
					+ context.request().uri()
					+ ") is different than the login URL (" + loginUrl + ")");
			context.session().put(PlayAuthenticate.ORIGINAL_URL,
					context.request().uri());
		} else {
			Logger.debug("The path we are coming from is the Login URL - delete jumpback");
			context.session().remove(PlayAuthenticate.ORIGINAL_URL);
		}
		return context.session().get(ORIGINAL_URL);
	}

	public static void storeUser(final Session session, final AuthUser u) {
		session.put(PlayAuthenticate.USER_KEY, u.getId());
		session.put(PlayAuthenticate.PROVIDER_KEY, u.getProvider());
		if (u.expires() != AuthUser.NO_EXPIRATION) {
			session.put(EXPIRES_KEY, Long.toString(u.expires()));
		} else {
			session.remove(EXPIRES_KEY);
		}
	}

	public static boolean isLoggedIn(final Session session) {
		boolean ret = session.containsKey(USER_KEY) // user is set
				&& session.containsKey(PROVIDER_KEY); // provider is set
		ret &= AuthProvider.Registry.hasProvider(session.get(PROVIDER_KEY)); // this
																				// provider
																				// is
																				// active
		if (session.containsKey(EXPIRES_KEY)) {
			// expiration is set
			final long expires = getExpiration(session);
			if (expires != AuthUser.NO_EXPIRATION) {
				ret &= (new Date()).getTime() < expires; // and the session
															// expires after now
			}
		}
		return ret;
	}

	public static void logout(final Session session) {
		session.remove(USER_KEY);
		session.remove(PROVIDER_KEY);
		session.remove(EXPIRES_KEY);

		// shouldn't be in any more, but just in case
		session.remove(ORIGINAL_URL);
	}

	public static String peekOriginalUrl(final Context context) {
		return context.session().get(ORIGINAL_URL);
	}

	public static boolean hasUserService() {
		return userService != null;
	}

	private static long getExpiration(final Session session) {
		long expires;
		if (session.containsKey(EXPIRES_KEY)) {
			try {
				expires = Long.parseLong(session.get(EXPIRES_KEY));
			} catch (final NumberFormatException nfe) {
				expires = AuthUser.NO_EXPIRATION;
			}
		} else {
			expires = AuthUser.NO_EXPIRATION;
		}
		return expires;
	}

	public static AuthUser getUser(final Session session) {
		final String provider = session.get(PROVIDER_KEY);
		final String id = session.get(USER_KEY);
		final long expires = getExpiration(session);

		if (provider != null && id != null) {
			return new SessionAuthUser(provider, id, expires);
		} else {
			return null;
		}
	}

	public static AuthUser getUser(final Context context) {
		return getUser(context.session());
	}

	public static boolean isAccountAutoMerge() {
		return getConfiguration().getBoolean("accountAutoMerge", false);
	}

	public static boolean isAccountAutoLink() {
		return getConfiguration().getBoolean("accountAutoLink", false);
	}
	
	public static boolean isAccountMergeEnabled() {
		return getConfiguration().getBoolean("accountMergeEnabled",true);
	}

	private static String getPlayAuthSessionId(final Session session) {
		// Generate a unique id
		String uuid = session.get(SESSION_ID_KEY);
		if (uuid == null) {
			uuid = java.util.UUID.randomUUID().toString();
			session.put(SESSION_ID_KEY, uuid);
		}
		return uuid;
	}

	private static void storeUserInCache(final Session session,
			final String key, final AuthUser identity) {
		play.cache.Cache.set(getCacheKey(session, key), identity);
	}

	private static void removeFromCache(final Session session, final String key) {
		play.cache.Cache.remove(getCacheKey(session, key));
	}

	private static String getCacheKey(final Session session, final String key) {
		final String id = getPlayAuthSessionId(session);
		return id + "_" + key;
	}

	private static AuthUser getUserFromCache(final Session session,
			final String key) {

		final Object o = play.cache.Cache.get(getCacheKey(session, key));
		if (o != null && o instanceof AuthUser) {
			return (AuthUser) o;
		}
		return null;
	}

	public static void storeMergeUser(final AuthUser identity,
			final Session session) {
		storeUserInCache(session, MERGE_USER_KEY, identity);
	}

	public static AuthUser getMergeUser(final Session session) {
		return getUserFromCache(session, MERGE_USER_KEY);
	}

	public static void removeMergeUser(final Session session) {
		removeFromCache(session, MERGE_USER_KEY);
	}

	public static void storeLinkUser(final AuthUser identity,
			final Session session) {
		storeUserInCache(session, LINK_USER_KEY, identity);
	}

	public static AuthUser getLinkUser(final Session session) {
		return getUserFromCache(session, LINK_USER_KEY);
	}

	public static void removeLinkUser(final Session session) {
		removeFromCache(session, LINK_USER_KEY);
	}

	private static String getJumpUrl(final Context ctx) {
		final String originalUrl = getOriginalUrl(ctx);
		if (originalUrl != null) {
			return originalUrl;
		} else {
			// this can be null if the user did not correctly define the
			// resolver
			final Call c = getResolver().afterAuth();
			if (c != null) {
				return c.url();
			} else {
				// go to root instead, but log this
				Logger.warn("Resolver did not contain information about where to go after authentication - redirecting to /");
				final String afterAuthFallback = getConfiguration().getString(
						"afterAuthFallback");
				if (afterAuthFallback != null && !afterAuthFallback.equals("")) {
					return afterAuthFallback;
				}
				
				// Not even the config setting was there or valid...meh
				Logger.error("Config setting 'afterAuthFallback' was not present!");
				return "/";
			}
		}
	}

	public static Result link(final Context context, final boolean link) {
		final AuthUser linkUser = getLinkUser(context.session());

		if (linkUser == null) {
			return Controller.forbidden();
		}

		final AuthUser loginUser;
		if (link) {
			// User accepted link - add account to existing local user
			loginUser = getUserService().link(getUser(context.session()),
					linkUser);
		} else {
			// User declined link - create new user
			try {
				loginUser = signupUser(linkUser);
			} catch (final AuthException e) {
				return Controller.internalServerError(e.getMessage());
			}
		}
		removeLinkUser(context.session());
		return loginAndRedirect(context, loginUser);
	}

	public static Result loginAndRedirect(final Context context,
			final AuthUser loginUser) {
		storeUser(context.session(), loginUser);
		return Controller.redirect(getJumpUrl(context));
	}

	public static Result merge(final Context context, final boolean merge) {
		final AuthUser mergeUser = getMergeUser(context.session());

		if (mergeUser == null) {
			return Controller.forbidden();
		}
		final AuthUser loginUser;
		if (merge) {
			// User accepted merge, so do it
			loginUser = getUserService().merge(mergeUser,
					getUser(context.session()));
		} else {
			// User declined merge, so log out the old user, and log out with
			// the new one
			loginUser = mergeUser;
		}
		removeMergeUser(context.session());
		return loginAndRedirect(context, loginUser);
	}

	public static AuthUser signupUser(final AuthUser u) throws AuthException {
		final AuthUser loginUser;
		final Object id = getUserService().save(u);
		if (id == null) {
			throw new AuthException("Could not sign you up");
		}
		loginUser = u;
		return loginUser;
	}
}

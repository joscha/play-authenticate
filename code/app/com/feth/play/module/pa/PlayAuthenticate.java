package com.feth.play.module.pa;

import java.util.Date;

import play.Configuration;
import play.Logger;
import play.Play;
import play.i18n.Messages;
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

public abstract class PlayAuthenticate {

	private static final String SETTING_KEY_PLAY_AUTHENTICATE = "play-authenticate";
	private static final String SETTING_KEY_AFTER_AUTH_FALLBACK = "afterAuthFallback";
	private static final String SETTING_KEY_AFTER_LOGOUT_FALLBACK = "afterLogoutFallback";
	private static final String SETTING_KEY_ACCOUNT_MERGE_ENABLED = "accountMergeEnabled";
	private static final String SETTING_KEY_ACCOUNT_AUTO_LINK = "accountAutoLink";
	private static final String SETTING_KEY_ACCOUNT_AUTO_MERGE = "accountAutoMerge";

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

		/**
		 * Route to redirect to after logout has been finished.
		 * If you return null here, the user will get redirected to the URL of
		 * the setting
		 * afterLogoutFallback
		 * You can use this to redirect to an external URL for example.
		 * 
		 * @return
		 */
		public abstract Call afterLogout();

		public Call onException(final AuthException e) {
			return null;
		}
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
			throw new RuntimeException(
					Messages.get("playauthenticate.core.exception.no_user_service"));
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
				.getConfig(SETTING_KEY_PLAY_AUTHENTICATE);
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

	public static void storeUser(final Session session, final AuthUser authUser) {

		// User logged in once more - wanna make some updates?
		final AuthUser u = getUserService().update(authUser);

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

	public static Result logout(final Session session) {
		session.remove(USER_KEY);
		session.remove(PROVIDER_KEY);
		session.remove(EXPIRES_KEY);

		// shouldn't be in any more, but just in case lets kill it from the
		// cookie
		session.remove(ORIGINAL_URL);

		return Controller.redirect(getUrl(getResolver().afterLogout(),
				SETTING_KEY_AFTER_LOGOUT_FALLBACK));
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
			return getProvider(provider).getSessionAuthUser(id, expires);
		} else {
			return null;
		}
	}

	public static AuthUser getUser(final Context context) {
		return getUser(context.session());
	}

	public static boolean isAccountAutoMerge() {
		return getConfiguration().getBoolean(SETTING_KEY_ACCOUNT_AUTO_MERGE);
	}

	public static boolean isAccountAutoLink() {
		return getConfiguration().getBoolean(SETTING_KEY_ACCOUNT_AUTO_LINK);
	}

	public static boolean isAccountMergeEnabled() {
		return getConfiguration().getBoolean(SETTING_KEY_ACCOUNT_MERGE_ENABLED);
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
		storeInCache(session, key, identity);
	}

	public static void storeInCache(final Session session, final String key,
			final Object o) {
		play.cache.Cache.set(getCacheKey(session, key), o);
	}

	public static Object removeFromCache(final Session session, final String key) {
		final Object o = getFromCache(session, key);
		
		final String k = getCacheKey(session, key);
		// TODO change on Play 2.1
		play.cache.Cache.set(k, null, 0);

		// POST-2.0/
		// play.cache.Cache.remove(k);
		return o;
	}

	private static String getCacheKey(final Session session, final String key) {
		final String id = getPlayAuthSessionId(session);
		return id + "_" + key;
	}

	public static Object getFromCache(final Session session, final String key) {
		return play.cache.Cache.get(getCacheKey(session, key));
	}

	private static AuthUser getUserFromCache(final Session session,
			final String key) {

		final Object o = getFromCache(session, key);
		if (o != null && o instanceof AuthUser) {
			return (AuthUser) o;
		}
		return null;
	}

	public static void storeMergeUser(final AuthUser identity,
			final Session session) {
		// TODO the cache is not ideal for this, because it might get cleared
		// any time
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
		// TODO the cache is not ideal for this, because it might get cleared
		// any time
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
			return getUrl(getResolver().afterAuth(),
					SETTING_KEY_AFTER_AUTH_FALLBACK);
		}
	}

	private static String getUrl(final Call c, final String settingFallback) {
		// this can be null if the user did not correctly define the
		// resolver
		if (c != null) {
			return c.url();
		} else {
			// go to root instead, but log this
			Logger.warn("Resolver did not contain information about where to go - redirecting to /");
			final String afterAuthFallback = getConfiguration().getString(
					settingFallback);
			if (afterAuthFallback != null && !afterAuthFallback.equals("")) {
				return afterAuthFallback;
			}
			// Not even the config setting was there or valid...meh
			Logger.error("Config setting '" + settingFallback
					+ "' was not present!");
			return "/";
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

	private static AuthUser signupUser(final AuthUser u) throws AuthException {
		final AuthUser loginUser;
		final Object id = getUserService().save(u);
		if (id == null) {
			throw new AuthException(
					Messages.get("playauthenticate.core.exception.singupuser_failed"));
		}
		loginUser = u;
		return loginUser;
	}

	public static Result handleAuthentication(final String provider,
			final Context context, final Object payload) {
		final AuthProvider ap = getProvider(provider);
		if (ap == null) {
			// Provider wasn't found and/or user was fooling with our stuff -
			// tell him off:
			return Controller.notFound(Messages.get(
					"playauthenticate.core.exception.provider_not_found",
					provider));
		}
		try {
			final Object o = ap.authenticate(context, payload);
			if (o instanceof String) {
				return Controller.redirect((String) o);
			} else if (o instanceof AuthUser) {

				final AuthUser newUser = (AuthUser) o;
				final Session session = context.session();

				// We might want to do merging here:
				// Adapted from:
				// http://stackoverflow.com/questions/6666267/architecture-for-merging-multiple-user-accounts-together
				// 1. The account is linked to a local account and no session
				// cookie is present --> Login
				// 2. The account is linked to a local account and a session
				// cookie is present --> Merge
				// 3. The account is not linked to a local account and no
				// session cookie is present --> Signup
				// 4. The account is not linked to a local account and a session
				// cookie is present --> Linking Additional account

				// get the user with which we are logged in - is null if we
				// are
				// not logged in (does NOT check expiration)

				AuthUser oldUser = getUser(session);

				// checks if the user is logged in (also checks the expiration!)
				boolean isLoggedIn = isLoggedIn(session);

				Object oldIdentity = null;

				// check if local user still exists - it might have been
				// deactivated/deleted,
				// so this is a signup, not a link
				if (isLoggedIn) {
					oldIdentity = getUserService().getLocalIdentity(oldUser);
					isLoggedIn &= oldIdentity != null;
					if (!isLoggedIn) {
						// if isLoggedIn is false here, then the local user has
						// been deleted/deactivated
						// so kill the session
						logout(session);
						oldUser = null;
					}
				}

				final Object loginIdentity = getUserService().getLocalIdentity(
						newUser);
				final boolean isLinked = loginIdentity != null;

				final AuthUser loginUser;
				if (isLinked && !isLoggedIn) {
					// 1. -> Login
					loginUser = newUser;

				} else if (isLinked && isLoggedIn) {
					// 2. -> Merge

					// merge the two identities and return the AuthUser we want
					// to use for the log in
					if (isAccountMergeEnabled()
							&& !loginIdentity.equals(oldIdentity)) {
						// account merge is enabled
						// and
						// The currently logged in user and the one to log in
						// are not the same, so shall we merge?

						if (isAccountAutoMerge()) {
							// Account auto merging is enabled
							loginUser = getUserService()
									.merge(newUser, oldUser);
						} else {
							// Account auto merging is disabled - forward user
							// to merge request page
							final Call c = getResolver().askMerge();
							if (c == null) {
								throw new RuntimeException(
										Messages.get(
												"playauthenticate.core.exception.merge.controller_undefined",
												SETTING_KEY_ACCOUNT_AUTO_MERGE));
							}
							storeMergeUser(newUser, session);
							return Controller.redirect(c);
						}
					} else {
						// the currently logged in user and the new login belong
						// to the same local user,
						// or Account merge is disabled, so just change the log
						// in to the new user
						loginUser = newUser;
					}

				} else if (!isLinked && !isLoggedIn) {
					// 3. -> Signup
					loginUser = signupUser(newUser);
				} else {
					// !isLinked && isLoggedIn:

					// 4. -> Link additional
					if (isAccountAutoLink()) {
						// Account auto linking is enabled

						loginUser = getUserService().link(oldUser, newUser);
					} else {
						// Account auto linking is disabled - forward user to
						// link suggestion page
						final Call c = getResolver().askLink();
						if (c == null) {
							throw new RuntimeException(
									Messages.get(
											"playauthenticate.core.exception.link.controller_undefined",
											SETTING_KEY_ACCOUNT_AUTO_LINK));
						}
						storeLinkUser(newUser, session);
						return Controller.redirect(c);
					}

				}

				return loginAndRedirect(context, loginUser);
			} else {
				return Controller.internalServerError(Messages
						.get("playauthenticate.core.exception.general"));
			}
		} catch (final AuthException e) {
			final Call c = getResolver().onException(e);
			if (c != null) {
				return Controller.redirect(c);
			} else {
				final String message = e.getMessage();
				if (message != null) {
					return Controller.internalServerError(message);
				} else {
					return Controller.internalServerError();
				}
			}
		}
	}

	public static AuthProvider getProvider(final String providerKey) {
		return AuthProvider.Registry.get(providerKey);
	}
}

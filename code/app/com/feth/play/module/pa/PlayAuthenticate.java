package com.feth.play.module.pa;

import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.service.UserService;
import com.feth.play.module.pa.user.AuthUser;
import com.typesafe.config.Config;
import play.Logger;
import play.cache.SyncCacheApi;
import play.i18n.Lang;
import play.i18n.MessagesApi;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Http.Session;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Singleton
public class PlayAuthenticate {

	public static final String SETTING_KEY_PLAY_AUTHENTICATE = "play-authenticate";
	private static final String SETTING_KEY_AFTER_AUTH_FALLBACK = "afterAuthFallback";
	private static final String SETTING_KEY_AFTER_LOGOUT_FALLBACK = "afterLogoutFallback";
	private static final String SETTING_KEY_ACCOUNT_MERGE_ENABLED = "accountMergeEnabled";
	private static final String SETTING_KEY_ACCOUNT_AUTO_LINK = "accountAutoLink";
	private static final String SETTING_KEY_ACCOUNT_AUTO_MERGE = "accountAutoMerge";

	private List<Lang> preferredLangs;
	private Config config;

	@Inject
	public PlayAuthenticate(final Config config, final Resolver resolver, final MessagesApi messagesApi, final SyncCacheApi cacheApi) {
		this.config = config;
		this.resolver = resolver;
		this.messagesApi = messagesApi;
		this.cacheApi = cacheApi;

		Locale englishLocale = new Locale("en");
		Lang englishLang = new Lang(englishLocale);
		preferredLangs = Arrays.asList(englishLang);
	}

	private Resolver resolver;
	private final MessagesApi messagesApi;
	private final SyncCacheApi cacheApi;

	public Resolver getResolver() {
		return resolver;
	}

	private UserService userService;

	public void setUserService(final UserService service) {
		userService = service;
	}

	public UserService getUserService() {
		if (userService == null) {
			throw new RuntimeException(
					messagesApi.preferred(preferredLangs).at("playauthenticate.core.exception.no_user_service"));
		}
		return userService;
	}

	private static final String ORIGINAL_URL = "pa.url.orig";
	private static final String USER_KEY = "pa.u.id";
	private static final String PROVIDER_KEY = "pa.p.id";
	private static final String EXPIRES_KEY = "pa.u.exp";
	private static final String SESSION_ID_KEY = "pa.s.id";

	public Config getConfiguration() {
		return config
				.getConfig(SETTING_KEY_PLAY_AUTHENTICATE);
	}

	public static final long TIMEOUT = 10L * 1000;
	private static final String MERGE_USER_KEY = null;
	private static final String LINK_USER_KEY = null;

	public String getOriginalUrl(final Http.Context context) {
		return context.session().remove(PlayAuthenticate.ORIGINAL_URL);
	}

	public String storeOriginalUrl(final Http.Context context) {
		String loginUrl = null;
		if (this.getResolver().login() != null) {
			loginUrl = this.getResolver().login().url();
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

	public void storeUser(final Session session, final AuthUser authUser) {

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

	public boolean isLoggedIn(final Session session) {
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

	public Result logout(final Session session) {
		session.remove(USER_KEY);
		session.remove(PROVIDER_KEY);
		session.remove(EXPIRES_KEY);

		// shouldn't be in any more, but just in case lets kill it from the
		// cookie
		session.remove(ORIGINAL_URL);

		return Controller.redirect(getUrl(getResolver().afterLogout(),
				SETTING_KEY_AFTER_LOGOUT_FALLBACK));
	}

	public String peekOriginalUrl(final Context context) {
		return context.session().get(ORIGINAL_URL);
	}

	public boolean hasUserService() {
		return userService != null;
	}

	private long getExpiration(final Session session) {
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

	public AuthUser getUser(final Session session) {
		final String provider = session.get(PROVIDER_KEY);
		final String id = session.get(USER_KEY);
		final long expires = getExpiration(session);

		if (provider != null && id != null) {
			return getProvider(provider).getSessionAuthUser(id, expires);
		} else {
			return null;
		}
	}

	public AuthUser getUser(final Context context) {
		return getUser(context.session());
	}

	public boolean isAccountAutoMerge() {
		return getConfiguration().getBoolean(SETTING_KEY_ACCOUNT_AUTO_MERGE);
	}

	public boolean isAccountAutoLink() {
		return getConfiguration().getBoolean(SETTING_KEY_ACCOUNT_AUTO_LINK);
	}

	public boolean isAccountMergeEnabled() {
		return getConfiguration().getBoolean(SETTING_KEY_ACCOUNT_MERGE_ENABLED);
	}

	private String getPlayAuthSessionId(final Session session) {
		// Generate a unique id
		String uuid = session.get(SESSION_ID_KEY);
		if (uuid == null) {
			uuid = java.util.UUID.randomUUID().toString();
			session.put(SESSION_ID_KEY, uuid);
		}
		return uuid;
	}

	private void storeUserInCache(final Session session,
			final String key, final AuthUser identity) {
		storeInCache(session, key, identity);
	}

	public void storeInCache(final Session session, final String key,
			final Object o) {
		cacheApi.set(getCacheKey(session, key), o);
	}

    public <T> T removeFromCache(final Session session, final String key) {
        final T o = getFromCache(session, key);

		final String k = getCacheKey(session, key);
		cacheApi.remove(k);
		return o;
	}

	private String getCacheKey(final Session session, final String key) {
		final String id = getPlayAuthSessionId(session);
		return id + "_" + key;
	}

    @SuppressWarnings("unchecked")
    public <T> T getFromCache(final Session session, final String key) {
        return (T) cacheApi.get(getCacheKey(session, key));
	}

	private AuthUser getUserFromCache(final Session session,
			final String key) {

		final Object o = getFromCache(session, key);
		if (o != null && o instanceof AuthUser) {
			return (AuthUser) o;
		}
		return null;
	}

	public void storeMergeUser(final AuthUser identity,
			final Session session) {
		// TODO the cache is not ideal for this, because it might get cleared
		// any time
		storeUserInCache(session, MERGE_USER_KEY, identity);
	}

	public AuthUser getMergeUser(final Session session) {
		return getUserFromCache(session, MERGE_USER_KEY);
	}

	public void removeMergeUser(final Session session) {
		removeFromCache(session, MERGE_USER_KEY);
	}

	public void storeLinkUser(final AuthUser identity,
			final Session session) {
		// TODO the cache is not ideal for this, because it might get cleared
		// any time
		storeUserInCache(session, LINK_USER_KEY, identity);
	}

	public AuthUser getLinkUser(final Session session) {
		return getUserFromCache(session, LINK_USER_KEY);
	}

	public void removeLinkUser(final Session session) {
		removeFromCache(session, LINK_USER_KEY);
	}

	private String getJumpUrl(final Context ctx) {
		final String originalUrl = getOriginalUrl(ctx);
		if (originalUrl != null) {
			return originalUrl;
		} else {
			return getUrl(getResolver().afterAuth(),
					SETTING_KEY_AFTER_AUTH_FALLBACK);
		}
	}

	private String getUrl(final Call c, final String settingFallback) {
		// this can be null if the user did not correctly define the
		// resolver
		if (c != null) {
			return c.url();
		} else {
			// go to root instead, but log this
			Logger.warn("Resolver did not contain information about where to go - redirecting to /");
			final String afterAuthFallback;
			if (getConfiguration().hasPath(settingFallback) && !(afterAuthFallback = getConfiguration().getString(
					settingFallback)).isEmpty()) {
				return afterAuthFallback;
			}
			// Not even the config setting was there or valid...meh
			Logger.error("Config setting '" + settingFallback
					+ "' was not present!");
			return "/";
		}
	}

	public Result link(final Context context, final boolean link) {
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
				loginUser = signupUser(linkUser, context.session(), getProvider(linkUser.getProvider()));
			} catch (final AuthException e) {
				return Controller.internalServerError(e.getMessage());
			}
		}
		removeLinkUser(context.session());
		return loginAndRedirect(context, loginUser);
	}

	public Result loginAndRedirect(final Context context,
			final AuthUser loginUser) {
		storeUser(context.session(), loginUser);
		return Controller.redirect(getJumpUrl(context));
	}

	public Result merge(final Context context, final boolean merge) {
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

	private AuthUser signupUser(final AuthUser u, final Session session, final AuthProvider provider) throws AuthException {
        final Object id = getUserService().save(u);
		if (id == null) {
			throw new AuthException(
					messagesApi.preferred(preferredLangs).at("playauthenticate.core.exception.signupuser_failed"));
		}
        provider.afterSave(u, id, session);
		return u;
	}

	public Result handleAuthentication(final String provider,
			final Context context, final Object payload) {
		final AuthProvider ap = getProvider(provider);
		if (ap == null) {
			// Provider wasn't found and/or user was fooling with our stuff -
			// tell him off:
			return Controller.notFound(messagesApi.preferred(preferredLangs).at(
					"playauthenticate.core.exception.provider_not_found",
					provider));
		}
		try {
			final Object o = ap.authenticate(context, payload);
			if (o instanceof String) {
				return Controller.redirect((String) o);
			} else if (o instanceof Result) {
				return (Result) o;
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
					isLoggedIn = oldIdentity != null;
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

				} else if (isLinked) {
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
										messagesApi.preferred(preferredLangs).at(
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

				} else if (!isLoggedIn) {
					// 3. -> Signup
					loginUser = signupUser(newUser, session, ap);
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
									messagesApi.preferred(preferredLangs).at(
											"playauthenticate.core.exception.link.controller_undefined",
											SETTING_KEY_ACCOUNT_AUTO_LINK));
						}
						storeLinkUser(newUser, session);
						return Controller.redirect(c);
					}

				}

				return loginAndRedirect(context, loginUser);
			} else {
				return Controller.internalServerError(messagesApi
						.preferred(preferredLangs).at("playauthenticate.core.exception.general"));
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

	public AuthProvider getProvider(final String providerKey) {
		return AuthProvider.Registry.get(providerKey);
	}
}

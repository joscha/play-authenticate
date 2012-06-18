package com.feth.play.module.pa;

import java.util.Date;

import play.Configuration;
import play.Logger;
import play.Play;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Http.Session;

import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.providers.AuthUser;
import com.feth.play.module.pa.providers.AuthUserIdentity;
import com.feth.play.module.pa.service.UserService;

public abstract class PlayAuthenticate {

	public abstract static class Resolver {
		public abstract Call login();

		public abstract Call afterAuth();

		public abstract Call auth(final String provider);
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

	public static Configuration getConfiguration() {
		return Play.application().configuration()
				.getConfig("play-authenticate");
	}

	public static final Long TIMEOUT = 10l * 1000;

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
			return new AuthUser() {

				@Override
				public String getProvider() {
					return provider;
				}

				@Override
				public String getId() {
					return id;
				}

				@Override
				public long expires() {
					return expires;
				}
			};
		} else {
			return null;
		}		
	}

	public static AuthUser getUser(final Context context) {
		return getUser(context.session());
	}

	public static boolean isSuggestAccountMerge() {
		return getConfiguration().getBoolean("suggestAccountMerge", true);
	}
}

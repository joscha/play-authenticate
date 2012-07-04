package com.feth.play.module.pa.providers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Application;
import play.Configuration;
import play.Logger;
import play.Plugin;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.SessionAuthUser;

public abstract class AuthProvider extends Plugin {

	public abstract static class Registry {
		private static Map<String, AuthProvider> providers = new HashMap<String, AuthProvider>();

		public static void register(final String provider, final AuthProvider p) {
			final Object previous = providers.put(provider, p);
			if (previous != null) {
				Logger.warn("There are multiple AuthProviders registered for key '"
						+ provider + "'");
			}
		}

		public static void unregister(final String provider) {
			providers.remove(provider);
		}

		public static AuthProvider get(final String provider) {
			return providers.get(provider);
		}

		public static Collection<AuthProvider> getProviders() {
			return providers.values();
		}

		public static boolean hasProvider(final String provider) {
			return providers.containsKey(provider);
		}
	}

	private Application application;

	public AuthProvider(final Application app) {
		application = app;
	}

	protected Application getApplication() {
		return application;
	}

	@Override
	public void onStart() {

		final List<String> neededSettings = neededSettingKeys();
		if (neededSettings != null) {
			final Configuration c = getConfiguration();
			if (c == null) {
				throw new RuntimeException("No settings for provider '"
						+ getKey() + "' available at all!");
			}
			for (final String key : neededSettings) {
				final String setting = c.getString(key);
				if (setting == null || "".equals(setting)) {
					throw new RuntimeException("Provider '" + getKey()
							+ "' missing needed setting '" + key + "'");
				}
			}
		}

		Registry.register(getKey(), this);
	}

	@Override
	public void onStop() {
		Registry.unregister(getKey());
	}

	public String getUrl() {
		return PlayAuthenticate.getResolver().auth(getKey()).url();
	}

	protected String getAbsoluteUrl(final Request request) {
		return PlayAuthenticate.getResolver().auth(getKey())
				.absoluteURL(request);
	}

	public abstract String getKey();

	protected Configuration getConfiguration() {
		return PlayAuthenticate.getConfiguration().getConfig(getKey());
	}

	/**
	 * Returns either an AuthUser object or a String (URL)
	 * 
	 * @param context
	 * @param payload
	 *            Some arbitrary payload that shall get passed into the
	 *            authentication process
	 * @return
	 * @throws AuthException
	 */
	public abstract Object authenticate(final Context context,
			final Object payload) throws AuthException;

	protected List<String> neededSettingKeys() {
		return null;
	}

	public AuthUser getSessionAuthUser(final String id, final long expires) {
		return new SessionAuthUser(getKey(), id, expires);
	}

	public abstract boolean isExternal();

}

package com.feth.play.module.pa.providers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;

import play.Application;
import play.Configuration;
import play.Logger;
import play.Plugin;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

public abstract class AuthProvider extends Plugin {
	
	public abstract static class Registry {
		private static Map<String, AuthProvider> providers = new HashMap<String, AuthProvider>(1);
		
		public static void register(String provider, AuthProvider p) {
			final Object previous = providers.put(provider, p);
			if(previous != null) {
				Logger.warn("There are multiple AuthProviders registered for key '"+provider+"'");
			}
		}
		
		public static void unregister(String provider) {
			providers.remove(provider);
		}

		public static AuthProvider get(String provider) {
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
		return PlayAuthenticate.getResolver().auth(getKey()).absoluteURL(request);
	}
	
	public abstract String getKey();
	
	protected Configuration getConfiguration() {
		return PlayAuthenticate.getConfiguration().getConfig(getKey());
	}

	/**
	 * Returns either an AuthUser object or a String (URL)
	 * 
	 * @param context
	 * @return
	 * @throws AuthException
	 */
	public abstract Object authenticate(final Context context) throws AuthException;
	
}

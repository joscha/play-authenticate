package com.feth.play.module.pa.service;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.AuthUserIdentity;

import play.Application;
import play.Logger;
import play.Plugin;

public abstract class UserServicePlugin extends Plugin implements UserService {
	
	private Application application;
	
	public UserServicePlugin(final Application app) {
		application = app;
	}
	
	protected Application getApplication() {
		return application;
	}
	
	@Override
	public void onStart() {
		if(PlayAuthenticate.hasUserService()) {
			Logger.warn("A user service was already registered - replacing the old one, however this might hint to a configuration problem");
		}
		PlayAuthenticate.setUserService(this);
	}
	
	
	@Override
	public AuthUserIdentity find(final String provider, final String id) {
		return new AuthUserIdentity() {
			
			@Override
			public String getProvider() {
				return provider;
			}
			
			@Override
			public String getId() {
				return id;
			}
		};
	}
}
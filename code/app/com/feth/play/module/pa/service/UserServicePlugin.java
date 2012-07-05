package com.feth.play.module.pa.service;

import play.Application;
import play.Logger;
import play.Plugin;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

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
			Logger.warn("A user service was already registered - replacing the old one, " +
					"however this might hint to a configuration problem if this is a production environment.");
		}
		PlayAuthenticate.setUserService(this);
	}
	
	@Override
	public AuthUser update(AuthUser knownUser) {
		// Default: just do nothing when user logs in again
		return knownUser;
	}
}
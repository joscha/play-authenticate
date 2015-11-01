package com.feth.play.module.pa.service;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F;

public abstract class UserServiceImpl implements UserService {

    public UserServiceImpl(ApplicationLifecycle lifecycle) {

        // content from Plugin.onStart
        if (PlayAuthenticate.hasUserService()) {
            final String oldServiceClass = PlayAuthenticate.getUserService().getClass().getName();

            Logger.warn("A user service was already registered - replacing the old one (" + oldServiceClass +
                    ") with the new one (" + getClass().getName() + "), " +
                    "however this might hint to a configuration problem if this is a production environment.");
        }

        PlayAuthenticate.setUserService(this);

        lifecycle.addStopHook(() -> {
            // content from Plugin.onStop
            return F.Promise.pure(null);
        });
    }

    @Override
    public AuthUser update(AuthUser knownUser) {
        // Default: just do nothing when user logs in again
        return knownUser;
    }
}

package service;

import models.AuthenticateUser;
import models.AuthenticateUser;
import play.Application;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.service.UserServicePlugin;

public class MyUserServicePlugin extends UserServicePlugin {

    public MyUserServicePlugin(final Application app) {
        super(app);
    }

    @Override
    public Object save(final AuthUser authUser) {
        final boolean isLinked = AuthenticateUser.existsByAuthUserIdentity(authUser);
        if (!isLinked) {
            return AuthenticateUser.create(authUser).id;
        } else {
            // we have this user already, so return null
            return null;
        }
    }

    @Override
    public Object getLocalIdentity(final AuthUserIdentity identity) {
        // For production: Caching might be a good idea here...
        // ...and dont forget to sync the cache when users get deactivated/deleted
        final AuthenticateUser u = AuthenticateUser.findByAuthUserIdentity(identity);
        if (u != null) {
            return u.id;
        } else {
            return null;
        }
    }

    @Override
    public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
        if (!oldUser.equals(newUser)) {
            AuthenticateUser.merge(oldUser, newUser);
        }
        return oldUser;
    }

    @Override
    public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
        AuthenticateUser.addLinkedAccount(oldUser, newUser);
        return newUser;
    }

    @Override
    public AuthUser update(final AuthUser knownUser) {
        // User logged in again, bump last login date
        AuthenticateUser.setLastLoginDate(knownUser);
        return knownUser;
    }

}

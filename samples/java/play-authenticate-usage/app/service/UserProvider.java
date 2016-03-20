package service;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import models.User;
import org.jetbrains.annotations.Nullable;
import play.mvc.Http.Session;

import javax.inject.Inject;

/**
 * Service layer for User DB entity
 */
public class UserProvider {

    private final PlayAuthenticate auth;

    @Inject
    public UserProvider(final PlayAuthenticate auth) {
        this.auth = auth;
    }

    @Nullable
    public User getUser(Session session) {
        final AuthUser currentAuthUser = this.auth.getUser(session);
        final User localUser = User.findByAuthUserIdentity(currentAuthUser);
        return localUser;
    }
}

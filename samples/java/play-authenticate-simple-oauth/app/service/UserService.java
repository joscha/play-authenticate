package service;

import com.feth.play.module.pa.user.AuthUser;
import models.User;

/**
 * Service operating on User entity.
 */
public class UserService {
    public User getLocalUser(final AuthUser authUser) {
        return User.findByAuthUserIdentity(authUser);
    }
}

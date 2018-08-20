package service;

import models.SecurityRole;

import java.util.Arrays;

/**
 * Data initializer class.
 */
public class DataInitializer {
    public DataInitializer() {
        if (SecurityRole.find.query().findCount() == 0) {
            for (final String roleName : Arrays
                    .asList(controllers.Application.USER_ROLE)) {
                final SecurityRole role = new SecurityRole();
                role.roleName = roleName;
                role.save();
            }
        }
    }
}

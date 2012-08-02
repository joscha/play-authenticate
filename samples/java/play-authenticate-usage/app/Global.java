import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import controllers.routes;
import models.AuthenticateSecurityRole;
import play.Application;
import play.GlobalSettings;
import play.mvc.Call;

import java.util.Arrays;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        PlayAuthenticate.setResolver(new Resolver() {

            @Override
            public Call login() {
                // Your login page
                return routes.Authenticate.login();
            }

            @Override
            public Call afterAuth() {
                // The user will be redirected to this page after authentication
                // if no original URL was saved
                return routes.Authenticate.index();
            }

            @Override
            public Call afterLogout() {
                return routes.Authenticate.index();
            }

            @Override
            public Call auth(final String provider) {
                // You can provide your own authentication implementation,
                // however the default should be sufficient for most cases
                return com.feth.play.module.pa.controllers.routes.Authenticate
                        .authenticate(provider);
            }

            @Override
            public Call askMerge() {
                return routes.AuthenticateAccount.askMerge();
            }

            @Override
            public Call askLink() {
                return routes.AuthenticateAccount.askLink();
            }

            @Override
            public Call onException(final AuthException e) {
                if (e instanceof AccessDeniedException) {
                    return routes.AuthenticateSignup
                            .oAuthDenied(((AccessDeniedException) e)
                                    .getProviderKey());
                }

                // more custom problem handling here...
                return super.onException(e);
            }
        });

        initialData();
    }

    private void initialData() {
        if (AuthenticateSecurityRole.find.findRowCount() == 0) {
            for (final String roleName : Arrays
                    .asList(controllers.Authenticate.USER_ROLE)) {
                final AuthenticateSecurityRole role = new AuthenticateSecurityRole();
                role.roleName = roleName;
                role.save();
            }
        }
    }
}
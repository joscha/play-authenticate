package service;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.google.inject.Inject;
import play.Configuration;
import play.mvc.Call;

/**
 * Created by rgupta on 03/11/15.
 */
public class TestSecurityConfig implements SecurityConfig {

    @Inject
    public TestSecurityConfig(Configuration config) {
        PlayAuthenticate.setResolver(new PlayAuthenticate.Resolver() {

            @Override
            public Call login() {
                // Your login page
                return controllers.routes.Application.login();
            }

            @Override
            public Call afterAuth() {
                // The user will be redirected to this page after authentication
                // if no original URL was saved
                return controllers.routes.Application.index();
            }

            @Override
            public Call afterLogout() {
                return controllers.routes.Application.index();
            }

            @Override
            public Call auth(final String provider) {
                // You can provide your own authentication implementation,
                // however the default should be sufficient for most cases
                return com.feth.play.module.pa.controllers.routes.Authenticate
                        .authenticate(provider);
            }

            @Override
            public Call onException(final AuthException e) {
                if (e instanceof AccessDeniedException) {
                    return controllers.routes.Application
                            .oAuthDenied(((AccessDeniedException) e)
                                    .getProviderKey());
                }

                // more custom problem handling here...

                return super.onException(e);
            }

            @Override
            public Call askLink() {
                // We don't support moderated account linking in this sample.
                // See the play-authenticate-usage project for an example
                return null;
            }

            @Override
            public Call askMerge() {
                // We don't support moderated account merging in this sample.
                // See the play-authenticate-usage project for an example
                return null;
            }
        });
    }
}

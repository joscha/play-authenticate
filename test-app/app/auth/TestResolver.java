package auth;

import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import play.mvc.Call;

import javax.inject.Singleton;

/**
 * Class used for initializing play-auth configuration.
 */
@Singleton
public class TestResolver extends Resolver {
    @Override
    public Call login() {
        // Your login page
        return controllers.routes.ApplicationController.login();
    }

    @Override
    public Call relogin() {
        // Your login page
        return controllers.routes.ApplicationController.index();
    }

    @Override
    public Call afterAuth() {
        // The user will be redirected to this page after authentication
        // if no original URL was saved
        return controllers.routes.ApplicationController.index();
    }

    @Override
    public Call afterLogout() {
        return controllers.routes.ApplicationController.index();
    }

    @Override
    public Call auth(final String provider) {
        // You can provide your own authentication implementation,
        // however the default should be sufficient for most cases
        return com.feth.play.module.pa.controllers.routes.Authenticate
                .authenticate(provider, false);
    }

    @Override
    public Call onException(final AuthException e) {
        if (e instanceof AccessDeniedException) {
            return controllers.routes.ApplicationController
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
}

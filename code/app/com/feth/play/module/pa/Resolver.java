package com.feth.play.module.pa;

import com.feth.play.module.pa.exceptions.AuthException;
import play.mvc.Call;

/**
 * Resolver abstract class. You need to provide your concrete implementation of this class in your app.
 * Example:
 * {@code
 *  bind(Resolver.class).to(TestResolver.class)
 * }
 */
public abstract class Resolver {

    /**
     * This is the route to your login page
     *
     * @return
     */
    public abstract Call login();

    /**
     * Route to redirect to after authentication has been finished.
     * Only used if no original URL was stored.
     * If you return null here, the user will get redirected to the URL of
     * the setting
     * afterAuthFallback
     * You can use this to redirect to an external URL for example.
     *
     * @return
     */
    public abstract Call afterAuth();

    /**
     * This should usually point to the route where you registered
     * com.feth.play.module.pa.controllers.AuthenticateController.
     * authenticate(String)
     * however you might provide your own authentication implementation if
     * you want to
     * and point it there
     *
     * @param provider
     *            The provider ID matching one of your registered providers
     *            in play.plugins
     *
     * @return a Call to follow
     */
    public abstract Call auth(final String provider);

    /**
     * If you set the accountAutoMerge setting to true, you might return
     * null for this.
     *
     * @return
     */
    public abstract Call askMerge();

    /**
     * If you set the accountAutoLink setting to true, you might return null
     * for this
     *
     * @return
     */
    public abstract Call askLink();

    /**
     * Route to redirect to after logout has been finished.
     * If you return null here, the user will get redirected to the URL of
     * the setting
     * afterLogoutFallback
     * You can use this to redirect to an external URL for example.
     *
     * @return
     */
    public abstract Call afterLogout();

    public Call onException(final AuthException e) {
        return null;
    }
}

package com.feth.play.module.pa.providers.cookie;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.inject.ApplicationLifecycle;
import play.mvc.Http.Context;
import play.mvc.Http.Cookie;
import play.mvc.Http.Cookies;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.user.AuthUser;

import javax.inject.Inject;

public abstract class CookieAuthProvider extends AuthProvider {
    private static final Logger log = LoggerFactory.getLogger(CookieAuthProvider.class);

    protected static final String PROVIDER_KEY = "cookie";

    protected static final String SESSION_KEY_DO_NOT_REMEMBER = "pa.cookie.do_not_remember";

    private static final String SETTING_KEY_COOKIE_NAME = "cookieName";
    private static final String SETTING_KEY_TIMEOUT = "timeout";
    private static final String SETTING_KEY_SECURE_ONLY = "secureOnly";
    private static final String SETTING_KEY_PATH = "path";

    @Override
    protected List<String> neededSettingKeys() {
        return Arrays.asList(
                SETTING_KEY_COOKIE_NAME,
                SETTING_KEY_TIMEOUT,
                SETTING_KEY_SECURE_ONLY,
                SETTING_KEY_PATH
        );
    }

    protected abstract CheckResult check(final CookieAuthUser cookieAuthUser);

    protected abstract void renew(final CookieAuthUser cookieAuthUser, final String newToken);


    @Inject
    public CookieAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle) {
        super(auth, lifecycle);
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    protected static enum CheckResult {
        SUCCESS,
        INVALID_TOKEN,
        MISSING_SERIES,
        EXPIRED,
        ERROR
    }

    protected abstract void save(final CookieAuthUser cookieAuthUser, final AuthUser loginUser);

    /**
     * Deletes an auth cookie. This assumes that any persisted auth cookie
     * with the given series (cookieAuthUser.getSeries()) is deleted.
     *
     * @param cookieAuthUser
     */
    protected void deleteSeries(final CookieAuthUser cookieAuthUser) {
        getAuth().getUserService().unlink(cookieAuthUser);
    }

    protected void potentialTheft(final CookieAuthUser cookieAuthUser) {
        log.warn("Potential cookie theft: {}", cookieAuthUser.getId());
    }

    protected String getRandom() {
        return UUID.randomUUID().toString();
    }

    protected String getNewSeries() {
        return getRandom();
    }

    protected String getNewToken() {
        return getRandom();
    }

    protected int getTimeout() {
        return getConfiguration().getInt(SETTING_KEY_TIMEOUT);
    }

    public String getCookieName() {
        return getConfiguration().getString(SETTING_KEY_COOKIE_NAME);
    }

    protected boolean isSecureOnly() {
        return !Play.isDev() && getConfiguration().getBoolean(SETTING_KEY_SECURE_ONLY);
    }

    protected String getPath() {
        return getConfiguration().getString(SETTING_KEY_PATH);
    }

    public void remember(final Context ctx) {
        remember(ctx, getAuth().getUser(ctx));
    }

    public void remember(final Context ctx, final AuthUser authUser) {
        if (authUser == null) {
            return;
        }

        if (Boolean.valueOf(ctx.session().get(SESSION_KEY_DO_NOT_REMEMBER))) {
            ctx.session().remove(SESSION_KEY_DO_NOT_REMEMBER);
            ctx.response().discardCookie(getCookieName());
            return;
        }

        CookieAuthUser cookieAuthUser = null;
        if (authUser instanceof CookieAuthUser) {
            final CookieAuthUser oldCookieAuthUser = (CookieAuthUser) authUser;
            cookieAuthUser = oldCookieAuthUser.renew(getNewToken());
            renew(oldCookieAuthUser, cookieAuthUser.getToken());
        } else {
            cookieAuthUser = new CookieAuthUser(getNewSeries(), getNewToken());
            save(cookieAuthUser, authUser);
        }

        ctx.response().setCookie(
                getCookieName(),
                cookieAuthUser.toCookieValue(),
                getTimeout(), // MaxAge in seconds
                getPath(), // Path
                null,
                isSecureOnly(),
                true); //httpOnly, not javascript!
    }

    public void forget(final Context ctx) {
        final AuthUser authUser = getAuth().getUser(ctx);
        ctx.response().discardCookie(getCookieName());
        if (authUser instanceof CookieAuthUser) {
            deleteSeries((CookieAuthUser)authUser);
        }
    }

    public CookieAuthUser authenticate(final Context ctx) {
        return (CookieAuthUser) authenticate(ctx, null);
    }

    @Override
    public Object authenticate(final Context ctx, final Object payload) {
        final Cookies cookies = ctx.request().cookies();
        final Cookie cookie = cookies.get(getCookieName());
        if (cookie == null) {
            log.trace("authenticate() - cookie ({}) is null", getCookieName());
            return null;
        }

        final CookieAuthUser cookieAuthUser = CookieAuthUser.fromCookieValue(cookie.value());
        if (cookieAuthUser == null) {
            log.warn("Discarding marlformed cookie: {}", cookie.value());
            ctx.response().discardCookie(getCookieName());
            return null;
        }

        final CheckResult checkResult = check(cookieAuthUser);

        if (null == checkResult ||
                CheckResult.MISSING_SERIES == checkResult ||
                CheckResult.EXPIRED == checkResult ||
                CheckResult.ERROR == checkResult) {

            log.debug("Discarding expired or missing ({}) cookie: {}",
                    (checkResult == null)? null : checkResult.name(), cookie.value());
            ctx.response().discardCookie(getCookieName());
            return null;

        } else if (CheckResult.SUCCESS == checkResult) {

            log.trace("Renew cookie: {}", cookie.value());
            remember(ctx, cookieAuthUser);
            getAuth().storeUser(ctx.session(), cookieAuthUser);
            return cookieAuthUser;

        } else if (CheckResult.INVALID_TOKEN == checkResult) {
            potentialTheft(cookieAuthUser);
            deleteSeries(cookieAuthUser);
            ctx.response().discardCookie(getCookieName());
            return null;

        }
        return null;
    }

    public void doNotRemember(Context ctx) {
        ctx.session().put(SESSION_KEY_DO_NOT_REMEMBER, "true");
    }

    @Override
    public boolean isExternal() {
        return false;
    }
}

package com.feth.play.module.pa.providers.cookie;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.TokenIdentity;

public class CookieAuthUser extends AuthUser implements TokenIdentity {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String series;
    private final String token;

    public CookieAuthUser(final String series, final String token) {
        this.series = series;
        this.token = token;
    }

    @Override
    public String getId() {
        return series;
    }

    @Override
    public String getProvider() {
        return CookieAuthProvider.PROVIDER_KEY;
    }

    public String getSeries() {
        return series;
    }

    @Override
    public String getToken() {
        return token;
    }

    public CookieAuthUser renew(final String newToken) {
        return new CookieAuthUser(series, newToken);
    }

    public String toCookieValue() {
        return String.format("%s|%s", series, token);
    }

    public static CookieAuthUser fromCookieValue(final String value) {
        final String[] fields = value.split("\\|");
        if (fields.length != 2) {
            return null;
        }
        return new CookieAuthUser(fields[0], fields[1]);
    }
}

package com.feth.play.module.pa.providers.oauth2.github;

import java.util.Collections;
import java.util.Map;

import play.Application;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.google.inject.Inject;

public class GithubAuthProvider extends
        OAuth2AuthProvider<GithubAuthUser, GithubAuthInfo> {

    public static final String PROVIDER_KEY = "github";

    private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";

    @Inject
    public GithubAuthProvider(Application app) {
        super(app);
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    protected Map<String, String> getHeaders() {
        return Collections.singletonMap("Accept", "application/json");
    }

    @Override
    protected GithubAuthUser transform(final GithubAuthInfo info, final String state)
            throws AuthException {

        final String url = getConfiguration().getString(
                USER_INFO_URL_SETTING_KEY);
        final WSResponse r = WS
                .url(url)
                .setQueryParameter(Constants.ACCESS_TOKEN,
                        info.getAccessToken()).get()
                .get(getTimeout());

        final JsonNode result = r.asJson();
        if (result.get(Constants.ERROR) != null) {
            throw new AuthException(result.get(
                    Constants.ERROR).asText());
        } else {
            return new GithubAuthUser(result, info, state);
        }
    }

    @Override
    protected GithubAuthInfo buildInfo(final WSResponse r)
            throws AccessTokenException {
        final JsonNode n = r.asJson();

        if (n.get(Constants.ERROR) != null) {
            throw new AccessTokenException(n.get(
                    Constants.ERROR).asText());
        } else {
            return new GithubAuthInfo(n);
        }
    }

}

package com.feth.play.module.pa.providers.oauth2.eventbrite;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Auth provider for Eventbrite https://www.eventbrite.com
 */
@Singleton
public class EventBriteAuthProvider extends
        OAuth2AuthProvider<EventBriteAuthUser, EventBriteAuthInfo> {

    public static final String PROVIDER_KEY = "eventbrite";

    private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";

    private static final String TOKEN = "token";

    @Inject
    public EventBriteAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final WSClient wsClient) {
        super(auth, lifecycle, wsClient);
    }


    @Override
    protected EventBriteAuthUser transform(final EventBriteAuthInfo info, final String state)
            throws AuthException {


        final String url = getConfiguration().getString(
                USER_INFO_URL_SETTING_KEY);

        final WSResponse r = fetchAuthResponse(url, new QueryParam(TOKEN, info.getAccessToken()));
        final JsonNode result = r.asJson();
        if (r.getStatus() >= 400) {
            throw new AuthException(result.get("meta").get("errorDetail").asText());
        } else {
            Logger.debug(result.toString());
            return new EventBriteAuthUser(result, info, state);
        }
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    protected EventBriteAuthInfo buildInfo(WSResponse r)
            throws AccessTokenException {
        if (r.getStatus() >= 400) {
            throw new AccessTokenException(r.toString());
        } else {
            final JsonNode result = r.asJson();
            Logger.debug(result.asText());
            return new EventBriteAuthInfo(result.get(
                    OAuth2AuthProvider.Constants.ACCESS_TOKEN).asText());
        }
    }
}

package com.feth.play.module.pa.providers.oauth2.facebook;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.typesafe.config.Config;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.i18n.MessagesApi;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http.Request;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class FacebookAuthProvider extends
        OAuth2AuthProvider<FacebookAuthUser, FacebookAuthInfo> {

    private static final String MESSAGE = "message";
    private static final String ERROR = "error";
    private static final String FIELDS = "fields";

    public static final String PROVIDER_KEY = "facebook";

    private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
    private static final String USER_INFO_FIELDS_SETTING_KEY = "userInfoFields";

    @Inject
    public FacebookAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final WSClient wsClient, final MessagesApi messagesApi) {
        super(auth, lifecycle, wsClient, messagesApi);
    }


    public static abstract class SettingKeys extends
            OAuth2AuthProvider.SettingKeys {
        public static final String DISPLAY = "display";
    }

    public static abstract class FacebookConstants extends OAuth2AuthProvider.Constants {
        public static final String DISPLAY = "display";
    }

    @Override
    protected FacebookAuthUser transform(FacebookAuthInfo info, final String state)
            throws AuthException {

        final String url = getConfiguration().getString(
                USER_INFO_URL_SETTING_KEY);
        final String fields = getConfiguration().getString(
                USER_INFO_FIELDS_SETTING_KEY);

        final WSResponse r = fetchAuthResponse(url,
                new QueryParam(OAuth2AuthProvider.Constants.ACCESS_TOKEN, info.getAccessToken()),
                new QueryParam(FIELDS, fields));

        final JsonNode result = r.asJson();
        if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
            throw new AuthException(result.get(ERROR).get(MESSAGE).asText());
        } else {
            Logger.debug(result.toString());
            return new FacebookAuthUser(result, info, state);
        }
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    protected FacebookAuthInfo buildInfo(final WSResponse r) throws AccessTokenException {

        final JsonNode respJson = r.asJson();

        if (r.getStatus() >= 400) {
            throw new AccessTokenException(r.asJson().get(ERROR).get(MESSAGE).asText());

        } else {

            if (respJson.size() < 2) {
                throw new AccessTokenException("At least two values were expected, but got " + respJson.size());
            }

            return new FacebookAuthInfo(respJson);
        }
    }

    @Override
    protected List<NameValuePair> getAuthParams(final Config c,
                                                final Request request, final String state) throws AuthException {
        final List<NameValuePair> params = super.getAuthParams(c, request, state);

        if (c.hasPath(SettingKeys.DISPLAY)) {
            params.add(new BasicNameValuePair(FacebookConstants.DISPLAY, c
                    .getString(SettingKeys.DISPLAY)));
        }

        return params;
    }
}

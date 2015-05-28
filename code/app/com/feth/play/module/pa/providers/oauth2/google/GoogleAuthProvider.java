package com.feth.play.module.pa.providers.oauth2.google;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.SessionAuthUser;
import com.google.common.base.Strings;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import play.Application;
import play.Configuration;
import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import play.mvc.Http;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GoogleAuthProvider extends
		OAuth2AuthProvider<GoogleAuthUser, GoogleAuthInfo> {

	public static final String PROVIDER_KEY = "google";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";

	public GoogleAuthProvider(Application app) {
		super(app);
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	protected GoogleAuthUser transform(final GoogleAuthInfo info, final String state)
			throws AuthException {

		final String url = getConfiguration().getString(
				USER_INFO_URL_SETTING_KEY);
		final WSResponse r = WS
				.url(url)
				.setQueryParameter(OAuth2AuthProvider.Constants.ACCESS_TOKEN,
						info.getAccessToken()).get()
				.get(getTimeout());

		final JsonNode result = r.asJson();
		if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
			throw new AuthException(result.get(
					OAuth2AuthProvider.Constants.ERROR).asText());
		} else {
			Logger.debug(result.toString());
			return new GoogleAuthUser(result, info, state);
		}
	}

	@Override
	protected GoogleAuthInfo buildInfo(final WSResponse r)
			throws AccessTokenException {
		final JsonNode n = r.asJson();
		Logger.debug(n.toString());

		if (n.get(OAuth2AuthProvider.Constants.ERROR) != null) {
			throw new AccessTokenException(n.get(
					OAuth2AuthProvider.Constants.ERROR).asText());
		} else {
			return new GoogleAuthInfo(n);
		}
	}

    @Override
    public AuthUser refresh(AuthUser authUser, Http.Session session) {
        Configuration config = PlayAuthenticate.getConfiguration().getConfig(PROVIDER_KEY);
        String url = config.getString(SettingKeys.REFRESH_TOKEN_URL);
        String refreshToken = PlayAuthenticate.getFromCache(session, OAuth2AuthProvider.Constants.REFRESH_TOKEN);
        if (Strings.isNullOrEmpty(refreshToken)){
            Logger.error("refresh token not found from cache");
            return null;
        }
        if(Strings.isNullOrEmpty(url)) {
            return null;
        }
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(OAuth2AuthProvider.Constants.REFRESH_TOKEN, refreshToken));
        params.add(new BasicNameValuePair(OAuth2AuthProvider.Constants.CLIENT_ID, config.getString(OAuth2AuthProvider.SettingKeys.CLIENT_ID)));
        params.add(new BasicNameValuePair(OAuth2AuthProvider.Constants.CLIENT_SECRET, config.getString(OAuth2AuthProvider.SettingKeys.CLIENT_SECRET)));
        params.add(new BasicNameValuePair(OAuth2AuthProvider.Constants.GRANT_TYPE, "refresh_token"));

        final WSRequestHolder request = WS
                .url(url);
        request.setContentType(ContentType.APPLICATION_FORM_URLENCODED.toString());
        WSResponse response = request.post(URLEncodedUtils.format(params, Charset.forName("UTF-8"))).get(getTimeout());
        AuthUser refreshedUser = null;
        if (response == null ) {
            Logger.error("[" + authUser.getId() + "] refresh token | fail | No response received for " + url);
        } else if(response.getStatus() != 200) {
            Logger.error("[" + authUser.getId() + "] refresh token | fail | HTTP_STATUS="+response.getStatus()+response.getBody());
        } else {
            JsonNode payload = response.asJson();
            JsonNode expiresNode = payload.get(OAuth2AuthProvider.Constants.EXPIRES_IN);
            if(expiresNode != null) {
                long expiresDate = new Date().getTime() + expiresNode.asLong() * 1000;
                refreshedUser = new SessionAuthUser(authUser.getProvider(), authUser.getId(), expiresDate);
                //update value in session
                PlayAuthenticate.storeUser(session, refreshedUser);
            }
        }
        return refreshedUser;
    }
}

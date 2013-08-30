package com.feth.play.module.pa.providers.oauth2.untappd;

import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.RedirectUriMismatch;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.user.AuthUserIdentity;
import org.codehaus.jackson.JsonNode;

import play.Application;
import play.Logger;
import play.libs.WS;
import play.libs.WS.Response;
import play.mvc.Http.Request;
import play.Configuration;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import play.mvc.Http.Context;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Auth provider for Untappd beer social network
 * https://www.untappd.com
 */
public class UntappdAuthProvider extends OAuth2AuthProvider<UntappdAuthUser, UntappdAuthInfo> {

	static final String PROVIDER_KEY = "untappd";

	private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
	private static final String ERROR = "error_type";
    private static final String REDIRECT_URL = "redirect_url";

    //Use this value for REDIRECT_URL in local development and put same URL in your Untappd App page
    //private static final String CALLBACK_URL = "http://localhost:9000/authenticate/untappd";

    public UntappdAuthProvider(final Application app) {
        super(app);
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    protected UntappdAuthUser transform(final UntappdAuthInfo info, final String state)
            throws AuthException {

        final String url = getConfiguration().getString(USER_INFO_URL_SETTING_KEY);

        final Response r = WS
                .url(url)
                .setQueryParameter(Constants.ACCESS_TOKEN, info.getAccessToken()).get()
                .get();

        final JsonNode result = r.asJson();
        if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
            throw new AuthException(result.get(
                    OAuth2AuthProvider.Constants.ERROR).asText());
        } else {
            return new UntappdAuthUser(result, info, state);
        }
    }

    @Override
    public Object authenticate(final Context context, final Object payload)
            throws AuthException {

        final Request request = context.request();

        final String error = Authenticate.getQueryString(request,ERROR);
        final String code = Authenticate.getQueryString(request, Constants.CODE);
        final String state = Authenticate.getQueryString(request,Constants.STATE);

        if (error != null) {
            Logger.info("Error = " + error);
            if (error.equals(Constants.ACCESS_DENIED)) {
                throw new AccessDeniedException(getKey());
            } else if (error.equals(Constants.REDIRECT_URI_MISMATCH)) {
                Logger.error("You must set the redirect URI for your provider to whatever you defined in your routes file."
                        + "For this provider it is: '"
                        + getRedirectUrl(request) + "'");
                throw new RedirectUriMismatch();
            } else {
                throw new AuthException(error);
            }
        } else if (code != null) {
            // second step in auth process
            final UntappdAuthInfo info = getAccessToken(code, request);
            final AuthUserIdentity u = transform(info, state);
            return u;

        } else {
            // no auth, yet
            final String url = getAuthUrl(request, state);
            return url;
        }
    }

    protected UntappdAuthInfo getAccessToken(final String code, final Request request)
            throws AccessTokenException {
        final Configuration c = getConfiguration();

        final String url = c.getString(SettingKeys.ACCESS_TOKEN_URL);

        final Response r = WS.url(url)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setQueryParameter(Constants.CLIENT_ID, c.getString(SettingKeys.CLIENT_ID))
                .setQueryParameter(Constants.CLIENT_SECRET, c.getString(SettingKeys.CLIENT_SECRET))
                .setQueryParameter(Constants.RESPONSE_TYPE, Constants.CODE)
                .setQueryParameter(Constants.CODE, code)
                .setQueryParameter(REDIRECT_URL, getRedirectUrl(request)).get()
                .get(PlayAuthenticate.TIMEOUT);

        return buildInfo(r);
    }


    private List<NameValuePair> getParams(final Request request,
                                          final Configuration c) {
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(Constants.CLIENT_ID, c.getString(SettingKeys.CLIENT_ID)));
        params.add(new BasicNameValuePair(Constants.CLIENT_SECRET, c.getString(SettingKeys.CLIENT_SECRET)));
        params.add(new BasicNameValuePair(REDIRECT_URL,getRedirectUrl(request)));

        return params;
    }

    protected String getAuthUrl(final Request request, final String state) {

        final Configuration c = getConfiguration();
        final List<NameValuePair> params = getParams(request, c);
        params.add(new BasicNameValuePair(Constants.RESPONSE_TYPE,Constants.CODE));

        if (c.getString(SettingKeys.ACCESS_TYPE) != null) {
            params.add(new BasicNameValuePair(Constants.ACCESS_TYPE, c.getString(SettingKeys.ACCESS_TYPE)));
        }

        if (c.getString(SettingKeys.APPROVAL_PROMPT) != null) {
            params.add(new BasicNameValuePair(Constants.APPROVAL_PROMPT, c.getString(SettingKeys.APPROVAL_PROMPT)));
        }

        if (state != null) {
            params.add(new BasicNameValuePair(Constants.STATE, state));
        }

        final HttpGet m = new HttpGet(c.getString(SettingKeys.AUTHORIZATION_URL) + "?"
                        + URLEncodedUtils.format(params, "UTF-8"));

        return m.getURI().toString();
    }


    @Override
    protected UntappdAuthInfo buildInfo(final Response r)
            throws AccessTokenException {
        final JsonNode n = r.asJson();

        if (n.get("meta").get("error_type") != null) {
            throw new AccessTokenException(n.get("meta").get("error_detail").asText());
        } else {
            return new UntappdAuthInfo(n.get("response").get(Constants.ACCESS_TOKEN).asText());
        }
    }

}

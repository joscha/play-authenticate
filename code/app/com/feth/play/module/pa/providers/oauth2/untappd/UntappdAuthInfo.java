package com.feth.play.module.pa.providers.oauth2.untappd;

import java.util.Date;

import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.Constants;

public class UntappdAuthInfo extends OAuth2AuthInfo {

    public UntappdAuthInfo(final String accessToken) {
        super(accessToken);
    }

}

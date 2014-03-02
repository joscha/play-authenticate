package com.feth.play.module.pa.providers.oauth2.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.Constants;

public class GithubAuthInfo extends OAuth2AuthInfo {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final String SCOPE = "scope";
    private String bearer;
    private String scope;

    public GithubAuthInfo(final JsonNode node) {
        super(node.get(Constants.ACCESS_TOKEN) != null ? node.get(Constants.ACCESS_TOKEN).asText() : null);

        bearer = node.get(Constants.TOKEN_TYPE).asText();
        scope = node.get(SCOPE).asText();
    }

    public String getBearer() {
        return bearer;
    }

    public String getScope() {
        return scope;
    }
}

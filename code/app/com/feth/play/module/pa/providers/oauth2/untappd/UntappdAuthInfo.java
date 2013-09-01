package com.feth.play.module.pa.providers.oauth2.untappd;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;

public class UntappdAuthInfo extends OAuth2AuthInfo {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UntappdAuthInfo(final String accessToken) {
        super(accessToken);
    }

}

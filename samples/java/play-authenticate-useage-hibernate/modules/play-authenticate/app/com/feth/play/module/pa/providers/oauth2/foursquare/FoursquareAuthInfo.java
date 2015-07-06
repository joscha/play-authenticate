package com.feth.play.module.pa.providers.oauth2.foursquare;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;

public class FoursquareAuthInfo extends OAuth2AuthInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FoursquareAuthInfo(final String accessToken) {
		super(accessToken);
	}
}

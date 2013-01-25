package com.feth.play.module.pa.providers.oauth2.facebook;

import java.util.Date;
import java.util.Map;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;

public class FacebookAuthInfo extends OAuth2AuthInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String EXPIRES = "expires";
	
	public FacebookAuthInfo(final Map<String, String> m) {
		super(m.get(OAuth2AuthProvider.Constants.ACCESS_TOKEN), new Date()
				.getTime()
				+ Long.parseLong(m.get(EXPIRES)) * 1000);
	}

}

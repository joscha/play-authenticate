package com.feth.play.module.pa.providers.oauth1.twitter;

import com.feth.play.module.pa.providers.oauth1.OAuth1AuthInfo;

public class TwitterAuthInfo extends OAuth1AuthInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TwitterAuthInfo(final String token, final String tokenSecret) {
		super(token, tokenSecret);
	}

}

package com.feth.play.module.pa.providers.oauth1.xing;

import com.feth.play.module.pa.providers.oauth1.OAuth1AuthInfo;

public class XingAuthInfo extends OAuth1AuthInfo {
	private static final long serialVersionUID = 1L;

	public XingAuthInfo(String token, String tokenSecret) {
		super(token, tokenSecret);
	}
}

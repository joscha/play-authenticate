package com.feth.play.module.pa.providers.oauth2.pocket;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.Constants;

import java.util.Map;

public class PocketAuthInfo extends OAuth2AuthInfo {

	private static final long serialVersionUID = 1L;
	private final String userName;

	public static final String USERNAME = "username";

	public PocketAuthInfo(final Map<String, String> map) {
		super(map.getOrDefault(Constants.ACCESS_TOKEN, null));
		this.userName = map.get(USERNAME);
	}

	public String getUserName() {
		return userName;
	}
}

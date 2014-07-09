package com.feth.play.module.pa.providers.oauth2.eventbrite;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;

public class EventBriteAuthInfo extends OAuth2AuthInfo {

	private static final long serialVersionUID = 1L;

	public EventBriteAuthInfo(final String accessToken) {
		super(accessToken);
	}

}

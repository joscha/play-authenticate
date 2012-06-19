package com.feth.play.module.pa.providers.oauth2.facebook;

import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthUser;

public class FacebookAuthUser extends OAuth2AuthUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static abstract class Constants {
		public static final String ID = "id"; // "616473731"
		public static final String NAME = "name"; // "Joscha Feth"
		public static final String FIRST_NAME = "first_name";// "Joscha"
		public static final String LAST_NAME = "last_name"; // "Feth"
		public static final String LINK = "link"; // "http://www.facebook.com/joscha.feth"
		public static final String USERNAME = "username";// "joscha.feth"
		public static final String GENDER = "gender";// "male"
		public static final String EMAIL = "email";// "joscha@feth.com"
		public static final String TIME_ZONE = "timezone";// 2
		public static final String LOCALE = "locale";// "de_DE"
		public static final String VERIFIED = "verified";// true
		public static final String UPDATE_TIME = "updated_time"; // "2012-04-26T20:22:52+0000"}
	}

	public FacebookAuthUser(final JsonNode node, final FacebookAuthInfo info) {
		super(node.get(Constants.ID).asText(), info);
	}

	@Override
	public String getProvider() {
		return FacebookAuthProvider.PROVIDER_KEY;
	}

}

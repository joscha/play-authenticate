package com.feth.play.module.pa.providers.oauth2.foursquare;

import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthUser;

public class FoursquareAuthUser extends OAuth2AuthUser {

	/**
	 * From:
	 * https://developer.foursquare.com/docs/responses/user
	 * 
	 */
	private abstract class Constants {
		public static final String ID = "id";
		public static final String FIRST_NAME = "firstName";
		public static final String LAST_NAME = "lastName";
		public static final String HOME_CITY = "homeCity";
		public static final String PHOTO = "photo";
		public static final String GENDER = "gender";
		public static final String RELATIONSHIP = "relationship";
	}

	public FoursquareAuthUser(final JsonNode node, final OAuth2AuthInfo info) {
		super(node.get(Constants.ID).asText(), info);
	}

	@Override
	public String getProvider() {
		return FoursquareAuthProvider.PROVIDER_KEY;
	}

}

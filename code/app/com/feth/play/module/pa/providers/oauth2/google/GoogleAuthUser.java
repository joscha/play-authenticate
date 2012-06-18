package com.feth.play.module.pa.providers.oauth2.google;

import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthUser;

public class GoogleAuthUser extends OAuth2AuthUser {

	/**
	 * From https://developers.google.com/accounts/docs/OAuth2Login#userinfocall
	 */
	public static class Constants {
		public static final String ID = "id"; // "00000000000000",
		public static final String EMAIL = "email"; // "fred.example@gmail.com",
		public static final String EMAIL_IS_VERIFIED = "verified_email"; // true,
		public static final String NAME = "name"; // "Fred Example",
		public static final String FIRST_NAME = "given_name"; // "Fred",
		public static final String LAST_NAME = "family_name"; // "Example",
		public static final String PICTURE = "picture"; // "https://lh5.googleusercontent.com/-2Sv-4bBMLLA/AAAAAAAAAAI/AAAAAAAAABo/bEG4kI2mG0I/photo.jpg",
		public static final String GENDER = "gender"; // "male",
		public static final String LOCALE = "locale"; // "en-US"
	}

	public GoogleAuthUser(final JsonNode n, final GoogleAuthInfo info) {
		super(n.get(Constants.ID).asText(), info);
	}

	@Override
	public String getProvider() {
		return GoogleAuthProvider.PROVIDER_KEY;
	}

}
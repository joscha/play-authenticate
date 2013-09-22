package com.feth.play.module.pa.providers.oauth2.google;

import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;

import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.ExtendedIdentity;
import com.feth.play.module.pa.user.LocaleIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;
import com.feth.play.module.pa.user.ProfiledIdentity;

public class GoogleAuthUser extends BasicOAuth2AuthUser implements
		ExtendedIdentity, PicturedIdentity, ProfiledIdentity, LocaleIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * From https://developers.google.com/accounts/docs/OAuth2Login#userinfocall
	 */
	private static class Constants {
		public static final String ID = "id"; // "00000000000000",
		public static final String EMAIL = "email"; // "fred.example@gmail.com",
		public static final String EMAIL_IS_VERIFIED = "verified_email"; // true,
		public static final String NAME = "name"; // "Fred Example",
		public static final String FIRST_NAME = "given_name"; // "Fred",
		public static final String LAST_NAME = "family_name"; // "Example",
		public static final String PICTURE = "picture"; // "https://lh5.googleusercontent.com/-2Sv-4bBMLLA/AAAAAAAAAAI/AAAAAAAAABo/bEG4kI2mG0I/photo.jpg",
		public static final String GENDER = "gender"; // "male",
		public static final String LOCALE = "locale"; // "en-US"
		public static final String LINK = "link"; // "https://plus.google.com/107424373956322297554"
	}

	private String email;
	private boolean emailIsVerified = false;
	private String name;
	private String firstName;
	private String lastName;
	private String picture;
	private String gender;
	private String locale;
	private String link;

	public GoogleAuthUser(final JsonNode n, final GoogleAuthInfo info,
			final String state) {
		super(n.get(Constants.ID).asText(), info, state);

		if (n.has(Constants.EMAIL)) {
			this.email = n.get(Constants.EMAIL).asText();
		}
		if (n.has(Constants.EMAIL_IS_VERIFIED)) {
			this.emailIsVerified = n.get(Constants.EMAIL_IS_VERIFIED)
					.asBoolean();
		}

		if (n.has(Constants.NAME)) {
			this.name = n.get(Constants.NAME).asText();
		}

		if (n.has(Constants.FIRST_NAME)) {
			this.firstName = n.get(Constants.FIRST_NAME).asText();
		}
		if (n.has(Constants.LAST_NAME)) {
			this.lastName = n.get(Constants.LAST_NAME).asText();
		}
		if (n.has(Constants.PICTURE)) {
			this.picture = n.get(Constants.PICTURE).asText();
		}
		if (n.has(Constants.GENDER)) {
			this.gender = n.get(Constants.GENDER).asText();
		}
		if (n.has(Constants.LOCALE)) {
			this.locale = n.get(Constants.LOCALE).asText();
		}
		if (n.has(Constants.LINK)) {
			this.link = n.get(Constants.LINK).asText();
		}
	}

	@Override
	public String getProvider() {
		return GoogleAuthProvider.PROVIDER_KEY;
	}

	public String getEmail() {
		return email;
	}

	public boolean isEmailVerified() {
		return emailIsVerified;
	}

	public String getName() {
		return name;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPicture() {
		return picture;
	}

	public String getGender() {
		return gender;
	}

	public String getProfileLink() {
		return link;
	}

	public Locale getLocale() {
		return AuthUser.getLocaleFromString(locale);
	}
}
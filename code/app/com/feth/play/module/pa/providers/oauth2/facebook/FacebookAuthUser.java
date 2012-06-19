package com.feth.play.module.pa.providers.oauth2.facebook;

import java.util.Locale;

import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthUser;
import com.feth.play.module.pa.user.ExtendedIdentity;
import com.feth.play.module.pa.user.LocaleIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;
import com.feth.play.module.pa.user.ProfiledIdentity;

public class FacebookAuthUser extends OAuth2AuthUser implements ExtendedIdentity, PicturedIdentity, ProfiledIdentity, LocaleIdentity {

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

	private final String name;
	private final String firstName;
	private final String lastName;
	private final String link;
	private final String username;
	private final String gender;
	private final String email;
	private final boolean verified;
	private final int timezone;
	private final String locale;
	private final String updateTime;

	public FacebookAuthUser(final JsonNode node, final FacebookAuthInfo info) {
		super(node.get(Constants.ID).asText(), info);

		this.name = node.get(Constants.NAME).asText();
		this.firstName = node.get(Constants.FIRST_NAME).asText();
		this.lastName = node.get(Constants.LAST_NAME).asText();
		this.link = node.get(Constants.LINK).asText();
		this.username = node.get(Constants.USERNAME).asText();
		this.gender = node.get(Constants.GENDER).asText();
		this.email = node.get(Constants.EMAIL).asText();
		this.verified = node.get(Constants.VERIFIED).asBoolean(false);
		this.timezone = node.get(Constants.TIME_ZONE).asInt();
		this.locale = node.get(Constants.LOCALE).asText();
		this.updateTime = node.get(Constants.UPDATE_TIME).asText();
	}

	@Override
	public String getProvider() {
		return FacebookAuthProvider.PROVIDER_KEY;
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

	public String getProfileLink() {
		return link;
	}

	public String getUsername() {
		return username;
	}

	public String getGender() {
		return gender;
	}

	public String getEmail() {
		return email;
	}

	public boolean isVerified() {
		return verified;
	}

	public int getTimezone() {
		return timezone;
	}
	
	public String getPicture() {
		// According to https://developers.facebook.com/docs/reference/api/#pictures
		return getProfileLink()+"/picture";
	}

	public Locale getLocale() {
		return new Locale(locale);
	}

	public String getUpdateTime() {
		return updateTime;
	}

	@Override
	public String toString() {
		return getName() + " ("+getEmail()+") @ "+getProvider();
	}
}

package com.feth.play.module.pa.providers.oauth1.twitter;

import java.util.Locale;

import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth1.BasicOAuth1AuthUser;
import com.feth.play.module.pa.providers.oauth1.OAuth1AuthInfo;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.ExtendedIdentity;
import com.feth.play.module.pa.user.LocaleIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;
import com.feth.play.module.pa.user.ProfiledIdentity;

public class TwitterAuthUser extends BasicOAuth1AuthUser implements
		ExtendedIdentity, PicturedIdentity, ProfiledIdentity, LocaleIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static abstract class Constants {

		public static final String ID = "id"; // "616473731"
		public static final String NAME = "name"; // "Joscha Feth"
		public static final String EMAIL = "email";// "joscha@feth.com"
		public static final String VERIFIED = "verified";// true
		private static final String PROFILE_IMAGE_URL = "profile_image_url";

	}

	private String name;
	private String firstName;
	private String lastName;
	private String link;
	private String username;
	private String gender;
	private String email;
	private boolean verified = false;
	private int timezone;
	private String locale;
	private String updateTime;
	private String picture;

	public TwitterAuthUser(final JsonNode node, final OAuth1AuthInfo info) {
		super(node.get(Constants.ID).asText(), info, null);

		if (node.has(Constants.NAME)) {
			this.name = node.get(Constants.NAME).asText();
		}
		if (node.has(Constants.EMAIL)) {
			this.email = node.get(Constants.EMAIL).asText();
		}
		if (node.has(Constants.VERIFIED)) {
			this.verified = node.get(Constants.VERIFIED).asBoolean(false);
		}
		if (node.has(Constants.PROFILE_IMAGE_URL)) {
			this.picture = node.get(Constants.PROFILE_IMAGE_URL).asText();
		}
	}

	@Override
	public String getProvider() {
		return TwitterAuthProvider.PROVIDER_KEY;
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
		return picture;
	}

	public Locale getLocale() {
		return AuthUser.getLocaleFromString(locale);
	}

	public String getUpdateTime() {
		return updateTime;
	}
}

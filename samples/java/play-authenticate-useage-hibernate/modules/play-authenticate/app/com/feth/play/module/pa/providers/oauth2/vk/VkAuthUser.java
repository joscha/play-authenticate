package com.feth.play.module.pa.providers.oauth2.vk;

import com.fasterxml.jackson.databind.JsonNode;

import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;
import com.feth.play.module.pa.user.ExtendedIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;

/**
 * @author Denis Borisenko
 */
public class VkAuthUser extends BasicOAuth2AuthUser implements
		ExtendedIdentity, PicturedIdentity {

	private static final long serialVersionUID = 1L;

	private static abstract class Constants {
		public static final String UID = "uid"; // 2037793
		public static final String FIRST_NAME = "first_name"; // "Денис"
		public static final String LAST_NAME = "last_name"; // "Борисенко"
		public static final String NICKNAME = "nickname"; // ''
		public static final String SCREEN_NAME = "screen_name"; // 'dborisenko'
		public static final String GENDER = "sex"; // 2
		public static final String BIRTH_DATE = "bdate"; // '9.2.1986'
		public static final String TIMEZONE = "timezone"; // 1
		public static final String PHOTO_50 = "photo_50";
		public static final String PHOTO_100 = "photo_100";
		public static final String PHOTO_200 = "photo_200_orig";
	}

	private String firstName;
	private String lastName;
	private String nickname;
	private String screenName;
	private String gender;
	private String birthDate;
	private int timezone;
	private String photo50;
	private String photo100;
	private String photo200;

	public VkAuthUser(final JsonNode node, final VkAuthInfo info,
			final String state) {
		super(node.get(Constants.UID).asText(), info, state);

		if (node.has(Constants.FIRST_NAME)) {
			this.firstName = node.get(Constants.FIRST_NAME).asText();
		}
		if (node.has(Constants.LAST_NAME)) {
			this.lastName = node.get(Constants.LAST_NAME).asText();
		}
		if (node.has(Constants.NICKNAME)) {
			this.nickname = node.get(Constants.NICKNAME).asText();
		}
		if (node.has(Constants.SCREEN_NAME)) {
			this.screenName = node.get(Constants.SCREEN_NAME).asText();
		}
		if (node.has(Constants.GENDER)) {
			int genderId = node.get(Constants.GENDER).asInt();
			if (genderId == 1)
				this.gender = "female";
			else if (genderId == 2)
				this.gender = "male";
		}
		if (node.has(Constants.BIRTH_DATE)) {
			this.birthDate = node.get(Constants.BIRTH_DATE).asText();
		}
		if (node.has(Constants.TIMEZONE)) {
			this.timezone = node.get(Constants.TIMEZONE).asInt();
		}
		if (node.has(Constants.PHOTO_50)) {
			this.photo50 = node.get(Constants.PHOTO_50).asText();
		}
		if (node.has(Constants.PHOTO_100)) {
			this.photo100 = node.get(Constants.PHOTO_100).asText();
		}
		if (node.has(Constants.PHOTO_200)) {
			this.photo200 = node.get(Constants.PHOTO_200).asText();
		}
	}

	@Override
	public String getEmail() {
		return null;
	}

	@Override
	public String getProvider() {
		return VkAuthProvider.PROVIDER_KEY;
	}

	@Override
	public String getName() {
		return getFirstName() + " " + getLastName();
	}

	@Override
	public String getGender() {
		return gender;
	}

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public String getPicture() {
		return getPhoto200();
	}

	public String getBirthDate() {
		return birthDate;
	}

	public String getNickname() {
		return nickname;
	}

	public String getPhoto100() {
		return photo100;
	}

	public String getPhoto200() {
		return photo200;
	}

	public String getPhoto50() {
		return photo50;
	}

	public String getScreenName() {
		return screenName;
	}

	public int getTimezone() {
		return timezone;
	}
}

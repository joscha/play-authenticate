package com.feth.play.module.pa.providers.oauth2.untappd;

import com.fasterxml.jackson.databind.JsonNode;

import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;
import com.feth.play.module.pa.user.BasicIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;

/**
 * https://untappd.com/api/docs#user_info
 */
public class UntappdAuthUser extends BasicOAuth2AuthUser implements
		BasicIdentity, FirstLastNameIdentity, PicturedIdentity {
	
	private static final long serialVersionUID = 1L;

	private static class Constants {
		public static final String EMAIL = "email_address";
		public static final String DISPLAY_NAME = "user_name";
		public static final String FIRST_NAME = "first_name";
		public static final String ID = "id";
		public static final String LAST_NAME = "last_name";
		public static final String AVATAR_URL = "user_avatar";
		public static final String SETTINGS = "settings";
	}
	
	private String displayName;
	private String email;
	private String firstName;
	private String avatar;
	private String lastName;

	public UntappdAuthUser(final JsonNode n, final UntappdAuthInfo info,
			final String state) {
		super(n.get(Constants.ID).asText(), info, state);

		if (n.has(Constants.FIRST_NAME)) {
			this.firstName = n.get(Constants.FIRST_NAME).asText();
		}
		if (n.has(Constants.LAST_NAME)) {
			this.lastName = n.get(Constants.LAST_NAME).asText();
		}
		if (n.has(Constants.DISPLAY_NAME)) {
			this.displayName = n.get(Constants.DISPLAY_NAME).asText();
		}
		if (n.has(Constants.SETTINGS)) {
			JsonNode settingsNode = n.get(Constants.SETTINGS);
			if (settingsNode.has(Constants.EMAIL)) {
				this.email = settingsNode.get(Constants.EMAIL).asText();
			}
		}
		if (n.has(Constants.AVATAR_URL)) {
			this.avatar = n.get(Constants.AVATAR_URL).asText();
		}
	}

	@Override
	public String getEmail() {
		return email;
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
	public String getName() {
		return displayName;
	}

	@Override
	public String getPicture() {
		return avatar;
	}

	@Override
	public String getProvider() {
		return UntappdAuthProvider.PROVIDER_KEY;
	}
}

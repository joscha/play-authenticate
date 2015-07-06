package com.feth.play.module.pa.providers.oauth1.xing;

import com.fasterxml.jackson.databind.JsonNode;

import com.feth.play.module.pa.providers.oauth1.BasicOAuth1AuthUser;
import com.feth.play.module.pa.providers.oauth1.OAuth1AuthInfo;
import com.feth.play.module.pa.user.ExtendedIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;
import com.feth.play.module.pa.user.ProfiledIdentity;

/**
 * See https://dev.xing.com/docs/get/users/me
 */
public class XingAuthUser extends BasicOAuth1AuthUser implements
		ExtendedIdentity, PicturedIdentity, ProfiledIdentity {
	private static final long serialVersionUID = 1L;

	private static final String ACTIVE_EMAIL = "active_email";
	private static final String DISPLAY_NAME = "display_name";
	private static final String FIRST_NAME = "first_name";
	private static final String GENDER = "gender";
	private static final String ID = "id";
	private static final String LAST_NAME = "last_name";
	private static final String PERMALINK = "permalink";
	private static final String PHOTO_URLS = "photo_urls";
	private static final String PHOTO_URLS_LARGE = "large";

	private String displayName;
	private String email;
	private String firstName;
	private String gender;
	private String largePhotoUrl;
	private String lastName;
	private String permalink;

	public XingAuthUser(final JsonNode nodeInfo, final OAuth1AuthInfo info) {
		// 'state' is always null?
		super(nodeInfo.has(ID) ? nodeInfo.get(ID).asText() : "N/A", info, null);

		if (nodeInfo.has(DISPLAY_NAME)) {
			this.displayName = nodeInfo.get(DISPLAY_NAME).asText();
		}
		if (nodeInfo.has(ACTIVE_EMAIL)) {
			this.email = nodeInfo.get(ACTIVE_EMAIL).asText();
		}
		if (nodeInfo.has(GENDER)) {
			this.gender = nodeInfo.get(GENDER).asText();
		}
		if (nodeInfo.has(FIRST_NAME)) {
			this.firstName = nodeInfo.get(FIRST_NAME).asText();
		}
		if (nodeInfo.has(LAST_NAME)) {
			this.lastName = nodeInfo.get(LAST_NAME).asText();
		}
		if (nodeInfo.has(PHOTO_URLS)) {
			final JsonNode photoUrlsNode = nodeInfo.get(PHOTO_URLS);
			if (photoUrlsNode.has(PHOTO_URLS_LARGE)) {
				this.largePhotoUrl = photoUrlsNode.get(PHOTO_URLS_LARGE)
						.asText();
			}
		}
		if (nodeInfo.has(PERMALINK)) {
			this.permalink = nodeInfo.get(PERMALINK).asText();
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
	public String getGender() {
		return gender;
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
		return largePhotoUrl;
	}

	@Override
	public String getProfileLink() {
		return permalink;
	}

	@Override
	public String getProvider() {
		return XingAuthProvider.PROVIDER_KEY;
	}
}

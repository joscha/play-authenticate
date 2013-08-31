package com.feth.play.module.pa.providers.oauth2.untappd;

import com.feth.play.module.pa.user.ExtendedIdentity;
import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;
import com.feth.play.module.pa.user.BasicIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;

/**
 * https://untappd.com/api/docs#user_info
 */
public class UntappdAuthUser extends BasicOAuth2AuthUser implements BasicIdentity,
		FirstLastNameIdentity, PicturedIdentity {
	private static final long serialVersionUID = 1L;

	private static final String EMAIL = "email_address";
	private static final String DISPLAY_NAME = "user_name";
	private static final String FIRST_NAME = "first_name";
	private static final String ID = "id";
	private static final String LAST_NAME = "last_name";
	private static final String AVATAR_URL = "user_avatar";
    private static final String SETTINGS = "settings";

	private String displayName;
	private String email;
	private String firstName;
	private String avatar;
	private String lastName;

	public UntappdAuthUser(final JsonNode n, final UntappdAuthInfo info,
                           final String state) {
        super(n.get("response").get("user").get(ID).asText(), info, state);

        JsonNode user = n.get("response").get("user");
		if (user.has(FIRST_NAME)) {
			this.firstName = user.get(FIRST_NAME).asText();
		}
		if (user.has(LAST_NAME)) {
			this.lastName = user.get(LAST_NAME).asText();
		}
		if (user.has(DISPLAY_NAME)) {
			this.displayName = user.get(DISPLAY_NAME).asText();
		}
        if (user.has(SETTINGS))
        {
            JsonNode settingsNode = user.get(SETTINGS);
            if (settingsNode.has(EMAIL)) {
                this.email = settingsNode.get(EMAIL).asText();
            }
        }
		if (user.has(AVATAR_URL)) {
            this.avatar = user.get(AVATAR_URL).asText();
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

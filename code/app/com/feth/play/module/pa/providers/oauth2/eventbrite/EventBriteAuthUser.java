package com.feth.play.module.pa.providers.oauth2.eventbrite;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import play.Logger;

public class EventBriteAuthUser extends BasicOAuth2AuthUser implements FirstLastNameIdentity {

	private static final long serialVersionUID = 1L;

	private static abstract class Constants {
		public static final String ID = "id";
		public static final String NAME = "name";
		public static final String FIRST_NAME = "first_name";
		public static final String LAST_NAME = "last_name";
		public static final String EMAILS_EMAIL = "email";
		public static final String EMAILS = "emails";
		public static final String PRIMARY = "primary";
        public static final String EMAILS_EMAIL_VERIFIED = "verified";
	}

	private String name;
	private String firstName;
	private String lastName;
	private String email;
	private boolean verified = false;

	public EventBriteAuthUser(final JsonNode node,
			final EventBriteAuthInfo info, final String state) {
		super(node.get(Constants.ID).asText(), info, state);

		if (node.has(Constants.FIRST_NAME)) {
			this.firstName = node.get(Constants.FIRST_NAME).asText();
		}
		if (node.has(Constants.LAST_NAME)) {
			this.lastName = node.get(Constants.LAST_NAME).asText();
		}
		if (node.has(Constants.NAME)) {
			this.name = node.get(Constants.NAME).asText();
		}

		if (node.has(Constants.EMAILS)) {
			final JsonNode eMailsNode = node.get(Constants.EMAILS);
            Logger.debug("Emails : " + eMailsNode);
			for (final JsonNode jsonNode : eMailsNode) {
				if (jsonNode.get(Constants.PRIMARY).asBoolean(false)) {
					this.email = jsonNode.get(Constants.EMAILS_EMAIL).asText();
                    this.verified = jsonNode.get(Constants.EMAILS_EMAIL_VERIFIED).asBoolean();
                    Logger.debug("Found primary email: " + this.email);
					break;
				} else if(this.email == null) {
                    this.email = jsonNode.get(Constants.EMAILS_EMAIL).asText();
                    this.verified = jsonNode.get(Constants.EMAILS_EMAIL_VERIFIED).asBoolean();
                    Logger.debug("First email: " + this.email);
                }
			}
		}

	}

	@Override
	public String getProvider() {
		return EventBriteAuthProvider.PROVIDER_KEY;
	}

    @Override
	public String getName() {
		return name;
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
	public String getEmail() {
		return email;
	}

	public boolean isVerified() {
		return verified;
	}

}

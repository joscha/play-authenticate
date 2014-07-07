package com.feth.play.module.pa.providers.oauth2.eventbrite;

import play.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;

public class EventBriteAuthUser extends BasicOAuth2AuthUser {

	private static final long serialVersionUID = 1L;

	private static abstract class Constants {
		public static final String ID = "id";
		public static final String NAME = "name";
		public static final String FIRST_NAME = "first_name";
		public static final String LAST_NAME = "last_name";
		public static final String EMAIL = "email";
		public static final String EMAILS = "emails";
		public static final String PRIMARY = "primary";
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
		Logger.debug("Emails : " + node.get(Constants.EMAILS));
		if (node.has(Constants.EMAILS)) {
			JsonNode settingsNode = node.get(Constants.EMAILS);
			Logger.debug("Settings Node : " + settingsNode);
			Logger.debug("Has email node : "
					+ settingsNode.has(Constants.EMAIL));
			for (JsonNode jsonNode : settingsNode) {
				Logger.debug("Verified "
						+ jsonNode.get(Constants.PRIMARY).asBoolean());
				if (jsonNode.get(Constants.PRIMARY).asBoolean()) {
					Logger.debug(jsonNode.get(Constants.EMAIL).asText());
					this.email = jsonNode.get(Constants.EMAIL).asText();
					break;

				}
			}
		}

	}

	@Override
	public String getProvider() {
		return EventBriteAuthProvider.PROVIDER_KEY;
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

	public String getEmail() {
		return email;
	}

	public boolean isVerified() {
		return verified;
	}

}

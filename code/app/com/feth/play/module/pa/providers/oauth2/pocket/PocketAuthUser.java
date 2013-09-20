package com.feth.play.module.pa.providers.oauth2.pocket;

import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;

public class PocketAuthUser extends BasicOAuth2AuthUser implements
		EmailIdentity, FirstLastNameIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static abstract class Constants {
		public static final String ID = "id"; // "616473731"
		public static final String FIRST_NAME = "first_name";// "Joscha"
		public static final String LAST_NAME = "last_name"; // "Feth"
		public static final String USERNAME = "username";// "joscha.feth"
		public static final String EMAIL = "email";// "joscha@feth.com"
	}

	private String firstName;
	private String lastName;
	private String username;
	private String email;
	
	public PocketAuthUser(final JsonNode node, final PocketAuthInfo info,
			final String state) {
		super(node.get(Constants.ID).asText(), info, state);

		if (node.has(Constants.FIRST_NAME)) {
			this.firstName = node.get(Constants.FIRST_NAME).asText();
		}
		if (node.has(Constants.LAST_NAME)) {
			this.lastName = node.get(Constants.LAST_NAME).asText();
		}
		if (node.has(Constants.USERNAME)) {
			this.username = node.get(Constants.USERNAME).asText();
		}
		if (node.has(Constants.EMAIL)) {
			this.email = node.get(Constants.EMAIL).asText();
		}
	}

	@Override
	public String getProvider() {
		return PocketAuthProvider.PROVIDER_KEY;
	}

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
  public String getName() {
	  return null;
  }

}

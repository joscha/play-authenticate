package com.feth.play.module.pa.providers.oauth2.foursquare;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.user.ExtendedIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;

public class FoursquareAuthUser extends BasicOAuth2AuthUser implements
		ExtendedIdentity, PicturedIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * From:
	 * https://developer.foursquare.com/docs/responses/user
	 * 
	 */
	private abstract class Constants {
		public static final String ID = "id"; // "1188384"
		public static final String FIRST_NAME = "firstName"; // "Joscha"
		public static final String LAST_NAME = "lastName"; // "Feth"
		public static final String HOME_CITY = "homeCity"; // "Metzingen, Baden-Württemberg"
		public static final String PHOTO = "photo"; // "https://is0.4sqi.net/userpix_thumbs/HZGTZQNRLA21ZIAD.jpg"
		public static final String GENDER = "gender"; // "male"
		public static final String TYPE = "type"; // "user"
		public static final String CONTACT = "contact"; // {"email":
														// "joscha@feth.com",
														// "twitter":
														// "joschafeth",
														// "facebook":
														// "616473731"}
		public static final String BIO = "bio"; // "lalala"
	}

	public static final String CONTACT_DETAIL_EMAIL = "email";
	public static final String CONTACT_DETAIL_TWITTER = "contact";
	public static final String CONTACT_DETAIL_FACEBOOK = "contact";

	private String firstName;
	private String lastName;
	private String homeCity;
	private String picture;
	private String gender;
	private String type;
	private String bio;
	private final Map<String, String> contact;

	public FoursquareAuthUser(final JsonNode node, final OAuth2AuthInfo info,
			final String state) {
		super(node.get(Constants.ID).asText(), info, state);

		if (node.has(Constants.FIRST_NAME)) {
			this.firstName = node.get(Constants.FIRST_NAME).asText();
		}
		if (node.has(Constants.LAST_NAME)) {
			this.lastName = node.get(Constants.LAST_NAME).asText();
		}
		if (node.has(Constants.HOME_CITY)) {
			this.homeCity = node.get(Constants.HOME_CITY).asText();
		}
		if (node.has(Constants.PHOTO)) {
			this.picture = node.get(Constants.PHOTO).asText();
		}
		if (node.has(Constants.GENDER)) {
			this.gender = node.get(Constants.GENDER).asText();
		}
		if (node.has(Constants.TYPE)) {
			this.type = node.get(Constants.TYPE).asText();
		}
		if (node.has(Constants.BIO)) {
			this.bio = node.get(Constants.BIO).asText();
		}

		final JsonNode contactNode = node.get(Constants.CONTACT);
		if (contactNode != null) {
			final Map<String, String> m = new HashMap<String, String>(
					contactNode.size());
			final Iterator<Entry<String, JsonNode>> fieldIterator = contactNode
					.getFields();
			while (fieldIterator.hasNext()) {
				final Map.Entry<String, JsonNode> entry = fieldIterator.next();
				m.put(entry.getKey(), entry.getValue().asText());
			}
			this.contact = m;
		} else {
			this.contact = Collections.emptyMap();
		}

	}

	@Override
	public String getProvider() {
		return FoursquareAuthProvider.PROVIDER_KEY;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getHomeCity() {
		return homeCity;
	}

	public String getPicture() {
		return picture;
	}

	public String getGender() {
		return gender;
	}

	public String getType() {
		return type;
	}

	public String getBio() {
		return bio;
	}

	/**
	 * It is not guaranteed that an email is present for foursquare
	 */
	public String getEmail() {
		return getContactDetail(CONTACT_DETAIL_EMAIL);
	}

	public String getContactDetail(final String key) {
		return contact.get(key);
	}

	@Override
	public String getName() {
		final StringBuilder sb = new StringBuilder();
		final boolean hasFirstName = getFirstName() != null
				&& !getFirstName().isEmpty();
		final boolean hasLastName = getLastName() != null
				&& !getLastName().isEmpty();
		if (hasFirstName) {
			sb.append(getFirstName());
			if (hasLastName) {
				sb.append(" ");
			}
		}
		if (hasLastName) {
			sb.append(getLastName());
		}

		return sb.toString();
	}
}

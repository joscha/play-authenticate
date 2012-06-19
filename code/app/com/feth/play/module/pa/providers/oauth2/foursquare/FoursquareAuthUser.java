package com.feth.play.module.pa.providers.oauth2.foursquare;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthUser;
import com.feth.play.module.pa.user.ExtendedIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;

public class FoursquareAuthUser extends OAuth2AuthUser implements ExtendedIdentity, PicturedIdentity {

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
	
	private final String firstName;
	private final String lastName;
	private final String homeCity;
	private final String picture;
	private final String gender;
	private final String type;
	private final String bio;
	private final Map<String, String> contact;

	public FoursquareAuthUser(final JsonNode node, final OAuth2AuthInfo info) {
		super(node.get(Constants.ID).asText(), info);

		this.firstName = node.get(Constants.FIRST_NAME).asText();
		this.lastName = node.get(Constants.LAST_NAME).asText();
		this.homeCity = node.get(Constants.HOME_CITY).asText();
		this.picture = node.get(Constants.PHOTO).asText();
		this.gender = node.get(Constants.GENDER).asText();
		this.type = node.get(Constants.TYPE).asText();
		this.bio = node.get(Constants.BIO).asText();

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
		return getFirstName() + " " + getLastName();
	}

	@Override
	public String toString() {
		return getName() + " ("+getEmail()+") @ "+getProvider();
	}
}

package com.feth.play.module.pa.providers.oauth1.twitter;

import java.util.Locale;

import org.codehaus.jackson.JsonNode;

import com.feth.play.module.pa.providers.oauth1.BasicOAuth1AuthUser;
import com.feth.play.module.pa.providers.oauth1.OAuth1AuthInfo;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.LocaleIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;

public class TwitterAuthUser extends BasicOAuth1AuthUser implements
		PicturedIdentity, LocaleIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static abstract class Constants {

		// {
		public static final String ID = "id";
		// "id":15484335,
		// "listed_count":5,
		public static final String PROFILE_IMAGE_URL = "profile_image_url";
		// "profile_image_url":"http://a0.twimg.com/profile_images/57096786/j_48x48_normal.png",
		// "following":false,
		// "followers_count":118,
		// "location":"Sydney, Australia",
		// "contributors_enabled":false,
		// "profile_background_color":"C0DEED",
		// "time_zone":"Berlin",
		// "geo_enabled":true,
		// "utc_offset":3600,
		// "is_translator":false,
		public static final String NAME = "name";
		// "name":"Joscha Feth",
		// "profile_background_image_url":"http://a0.twimg.com/images/themes/theme1/bg.png",
		// "show_all_inline_media":false,
		public static final String SCREEN_NAME = "screen_name";
		// "screen_name":"joschafeth",
		// "protected":false,
		// "profile_link_color":"0084B4",
		// "default_profile_image":false,
		// "follow_request_sent":false,
		// "profile_background_image_url_https":"https://si0.twimg.com/images/themes/theme1/bg.png",
		// "favourites_count":3,
		// "notifications":false,
		public static final String VERIFIED = "verified";
		// "verified":false,
		// "profile_use_background_image":true,
		// "profile_text_color":"333333",
		// "description":"",
		// "id_str":"15484335",
		public static final String LOCALE = "lang";
		// "lang":"en",
		// "profile_sidebar_border_color":"C0DEED",
		// "profile_image_url_https":"https://si0.twimg.com/profile_images/57096786/j_48x48_normal.png",
		// "default_profile":true,
		// "url":null,
		// "statuses_count":378,
		// "status":{
		// "in_reply_to_user_id":11111,
		// "truncated":false,
		// "created_at":"Mon Jul 23 13:22:31 +0000 2012",
		// "coordinates":null,
		// "geo":null,
		// "favorited":false,
		// "in_reply_to_screen_name":"XXX",
		// "contributors":null,
		// "in_reply_to_status_id_str":"111111",
		// "place":null,
		// "source":"<a href=\"http://itunes.apple.com/us/app/twitter/id409789998?mt=12\" rel=\"nofollow\">Twitter for Mac</a>",
		// "in_reply_to_user_id_str":"11111",
		// "id":111111,
		// "id_str":"111111",
		// "retweeted":false,
		// "retweet_count":0,
		// "in_reply_to_status_id":11111,
		// "text":"some text to up to 140chars here"
		// },
		// "profile_background_tile":false,
		// "friends_count":120,
		// "created_at":"Fri Jul 18 18:17:46 +0000 2008",
		// "profile_sidebar_fill_color":"DDEEF6"

	}

	private String name;
	private String screenName;
	private boolean verified = false;
	private String locale;
	private String picture;

	public TwitterAuthUser(final JsonNode node, final OAuth1AuthInfo info) {
		super(node.get(Constants.ID).asText(), info, null);

		if (node.has(Constants.NAME)) {
			this.name = node.get(Constants.NAME).asText();
		}
		if (node.has(Constants.LOCALE)) {
			this.locale = node.get(Constants.LOCALE).asText();
		}
		if (node.has(Constants.SCREEN_NAME)) {
			this.screenName = node.get(Constants.SCREEN_NAME).asText();
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

	public String getScreenName() {
		return screenName;
	}

	public boolean isVerified() {
		return verified;
	}

	public String getPicture() {
		return picture;
	}

	public Locale getLocale() {
		return AuthUser.getLocaleFromString(locale);
	}
}

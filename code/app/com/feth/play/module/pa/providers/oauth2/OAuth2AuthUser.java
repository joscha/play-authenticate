package com.feth.play.module.pa.providers.oauth2;

import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;

import com.feth.play.module.pa.user.AuthUser;

public abstract class OAuth2AuthUser extends AuthUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private OAuth2AuthInfo info;
	private String id;

	private final String state;

	public OAuth2AuthUser(final String id, final OAuth2AuthInfo info,
			final String state) {
		this.info = info;
		this.id = id;
		this.state = state;
	}

	public OAuth2AuthInfo getOAuth2AuthInfo() {
		return info;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long expires() {
		return getOAuth2AuthInfo().getExpiration();
	}

	public String getState() {
		return state;
	}

	public static Locale getLocaleFromString(final String locale) {
		if (locale != null && !locale.isEmpty()) {
			try {
				return LocaleUtils.toLocale(locale);
			} catch (final java.lang.IllegalArgumentException iae) {
				try {
					return LocaleUtils.toLocale(locale.replace('-', '_'));
				} catch (final java.lang.IllegalArgumentException iae2) {
					return null;
				}
			}
		} else {
			return null;
		}
	}
}

package models;

import com.feth.play.module.pa.user.AuthUser;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class CookieTokenSeries extends AppModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@ManyToOne
	public User user;

	@OneToOne(targetEntity = LinkedAccount.class, mappedBy = "providerUserId")
	public String series;
	public String token;

	@Temporal(TemporalType.TIMESTAMP)
	public LocalDateTime timeCreated, timeUpdated;

	public static final Finder<Long, CookieTokenSeries> find = new Finder<Long, CookieTokenSeries>(CookieTokenSeries.class);

	public static CookieTokenSeries findBySeries(final User user, String key) {
		return find.where().eq("user", user).eq("series", key)
				.findUnique();
	}


//
	public static CookieTokenSeries create(final AuthUser user, String series, String token) {
		final CookieTokenSeries ret = new CookieTokenSeries();
		ret.series = series;
		ret.token = token;
		ret.timeCreated = LocalDateTime.now();
		ret.timeUpdated = LocalDateTime.now();
		ret.user = User.findByAuthUserIdentity(user);

		return ret;
	}

	public void updateToken(String token) {
		this.token = token;
		this.timeUpdated = LocalDateTime.now();
	}
}
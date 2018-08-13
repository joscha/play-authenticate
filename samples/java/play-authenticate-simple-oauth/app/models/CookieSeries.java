package models;

import com.feth.play.module.pa.user.AuthUser;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class CookieSeries extends AppModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public String series;
	public String token;

	public static final Finder<Long, CookieSeries> find = new Finder<Long, CookieSeries>(Long.class, CookieSeries.class);

	public static CookieSeries findByProviderKey(final User user, String key) {
		return find.where().eq("user", user).eq("providerKey", key)
				.findUnique();
	}


//
	public static CookieSeries create(String series, String token) {
		final CookieSeries ret = new CookieSeries();
		ret.series = series;
		ret.token = token;

		return ret;
	}
}
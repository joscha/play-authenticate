package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.format.Formats;
import play.db.ebean.Model;

import com.avaje.ebean.Ebean;

@Entity
public class UserActivation extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@Column(unique = true)
	public String token;

	@ManyToOne
	public User unverified;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date expires;

	public static final Finder<Long, UserActivation> find = new Finder<Long, UserActivation>(
			Long.class, UserActivation.class);

	public static UserActivation findByToken(final String token) {
		return find.where().eq("token", token).findUnique();
	}

	public static void deleteByUser(final User u) {
		Ebean.delete(find.where().eq("unverified.id", u.id).findIterate());
	}
}

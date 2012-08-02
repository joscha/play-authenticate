package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.annotation.EnumValue;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
public class AuthenticateTokenAction extends Model {

    public enum Type {
        @EnumValue("EV")
        EMAIL_VERIFICATION,

        @EnumValue("PR")
        PASSWORD_RESET
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Verification time frame (until the user clicks on the link in the email)
     * in seconds
     * Defaults to one week
     */
    private final static long VERIFICATION_TIME = 7 * 24 * 3600;

    @Id
    public Long id;

    @Column(unique = true)
    public String token;

    @ManyToOne
    public AuthenticateUser targetUser;

    public Type type;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date created;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date expires;

    public static final Finder<Long, AuthenticateTokenAction> find = new Finder<Long, AuthenticateTokenAction>(
            Long.class, AuthenticateTokenAction.class);

    public static AuthenticateTokenAction findByToken(final String token, final Type type) {
        return find.where().eq("token", token).eq("type", type).findUnique();
    }

    public static void deleteByUser(final AuthenticateUser u, final Type type) {
        Ebean.delete(find.where().eq("targetUser.id", u.id).eq("type", type)
                .findIterate());
    }

    public boolean isValid() {
        return this.expires.after(new Date());
    }

    public static AuthenticateTokenAction create(final Type type, final String token,
                                     final AuthenticateUser targetUser) {
        final AuthenticateTokenAction ua = new AuthenticateTokenAction();
        ua.targetUser = targetUser;
        ua.token = token;
        ua.type = type;
        final Date created = new Date();
        ua.created = created;
        ua.expires = new Date(created.getTime() + VERIFICATION_TIME * 1000);
        ua.save();
        return ua;
    }
}
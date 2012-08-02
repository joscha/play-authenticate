package models;

import com.feth.play.module.pa.user.AuthUser;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class AuthenticateLinkedAccount extends Model {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @ManyToOne
    public AuthenticateUser user;

    public String providerUserId;
    public String providerKey;

    public static final Finder<Long, AuthenticateLinkedAccount> find = new Finder<Long, AuthenticateLinkedAccount>(
            Long.class, AuthenticateLinkedAccount.class);

    public static AuthenticateLinkedAccount findByProviderKey(final AuthenticateUser user, String key) {
        return find.where().eq("user", user).eq("providerKey", key)
                .findUnique();
    }

    public static AuthenticateLinkedAccount create(final AuthUser authUser) {
        final AuthenticateLinkedAccount ret = new AuthenticateLinkedAccount();
        ret.update(authUser);
        return ret;
    }

    public void update(final AuthUser authUser) {
        this.providerKey = authUser.getProvider();
        this.providerUserId = authUser.getId();
    }

    public static AuthenticateLinkedAccount create(final AuthenticateLinkedAccount acc) {
        final AuthenticateLinkedAccount ret = new AuthenticateLinkedAccount();
        ret.providerKey = acc.providerKey;
        ret.providerUserId = acc.providerUserId;

        return ret;
    }
}
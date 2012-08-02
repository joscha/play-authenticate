package models;

import be.objectify.deadbolt.models.Permission;
import be.objectify.deadbolt.models.Role;
import be.objectify.deadbolt.models.RoleHolder;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.validation.Email;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import controllers.Authenticate;
import models.AuthenticateTokenAction.Type;
import play.data.format.Formats;
import play.db.ebean.Model;
import scala.actors.threadpool.Arrays;

import javax.persistence.*;
import java.util.*;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
@Table(name = "users")
public class AuthenticateUser extends Model implements RoleHolder {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Email
    // if you make this unique, keep in mind that users *must* merge/link their
    // accounts then on signup with additional providers
    // @Column(unique = true)
    public String email;

    public String name;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date lastLogin;

    public boolean active;

    public boolean emailValidated;

    @ManyToMany
    public List<AuthenticateSecurityRole> roles;

    @OneToMany(cascade = CascadeType.ALL)
    public List<AuthenticateLinkedAccount> linkedAccounts;

    @ManyToMany
    public List<AuthenticateUserPermission> permissions;

    public static final Finder<Long, AuthenticateUser> find = new Finder<Long, AuthenticateUser>(
            Long.class, AuthenticateUser.class);


    public List<? extends Role> getRoles() {
        return roles;
    }

    public List<? extends Permission> getPermissions() {
        return permissions;
    }

    public static boolean existsByAuthUserIdentity(
            final AuthUserIdentity identity) {
        final ExpressionList<AuthenticateUser> exp;
        if (identity instanceof UsernamePasswordAuthUser) {
            exp = getUsernamePasswordAuthUserFind((UsernamePasswordAuthUser) identity);
        } else {
            exp = getAuthUserFind(identity);
        }
        return exp.findRowCount() > 0;
    }

    private static ExpressionList<AuthenticateUser> getAuthUserFind(
            final AuthUserIdentity identity) {
        return find.where().eq("active", true)
                .eq("linkedAccounts.providerUserId", identity.getId())
                .eq("linkedAccounts.providerKey", identity.getProvider());
    }

    public static AuthenticateUser findByAuthUserIdentity(final AuthUserIdentity identity) {
        if (identity == null) {
            return null;
        }
        if (identity instanceof UsernamePasswordAuthUser) {
            return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
        } else {
            return getAuthUserFind(identity).findUnique();
        }
    }

    public static AuthenticateUser findByUsernamePasswordIdentity(
            final UsernamePasswordAuthUser identity) {
        return getUsernamePasswordAuthUserFind(identity).findUnique();
    }

    private static ExpressionList<AuthenticateUser> getUsernamePasswordAuthUserFind(
            final UsernamePasswordAuthUser identity) {
        return getEmailUserFind(identity.getEmail()).eq(
                "linkedAccounts.providerKey", identity.getProvider());
    }

    public void merge(final AuthenticateUser otherUser) {
        for (final AuthenticateLinkedAccount acc : otherUser.linkedAccounts) {
            this.linkedAccounts.add(AuthenticateLinkedAccount.create(acc));
        }
        // do all other merging stuff here - like resources, etc.

        // deactivate the merged user that got added to this one
        otherUser.active = false;
        Ebean.save(Arrays.asList(new AuthenticateUser[]{otherUser, this}));
    }

    public static AuthenticateUser create(final AuthUser authUser) {
        final AuthenticateUser user = new AuthenticateUser();
        user.roles = Collections.singletonList(AuthenticateSecurityRole
                .findByRoleName(Authenticate.USER_ROLE));
        // user.permissions = new ArrayList<AuthenticateUserPermission>();
        // user.permissions.add(AuthenticateUserPermission.findByValue("printers.edit"));
        user.active = true;
        user.lastLogin = new Date();
        user.linkedAccounts = Collections.singletonList(AuthenticateLinkedAccount
                .create(authUser));

        if (authUser instanceof EmailIdentity) {
            final EmailIdentity identity = (EmailIdentity) authUser;
            // Remember, even when getting them from FB & Co., emails should be
            // verified within the application as a security breach there might
            // break your security as well!
            user.email = identity.getEmail();
            user.emailValidated = false;
        }

        if (authUser instanceof NameIdentity) {
            final NameIdentity identity = (NameIdentity) authUser;
            final String name = identity.getName();
            if (name != null) {
                user.name = name;
            }
        }

        user.save();
        user.saveManyToManyAssociations("roles");
        // user.saveManyToManyAssociations("permissions");
        return user;
    }

    public static void merge(final AuthUser oldUser, final AuthUser newUser) {
        AuthenticateUser.findByAuthUserIdentity(oldUser).merge(
                AuthenticateUser.findByAuthUserIdentity(newUser));
    }

    public Set<String> getProviders() {
        final Set<String> providerKeys = new HashSet<String>(
                linkedAccounts.size());
        for (final AuthenticateLinkedAccount acc : linkedAccounts) {
            providerKeys.add(acc.providerKey);
        }
        return providerKeys;
    }

    public static void addLinkedAccount(final AuthUser oldUser,
                                        final AuthUser newUser) {
        final AuthenticateUser u = AuthenticateUser.findByAuthUserIdentity(oldUser);
        u.linkedAccounts.add(AuthenticateLinkedAccount.create(newUser));
        u.save();
    }

    public static void setLastLoginDate(final AuthUser knownUser) {
        final AuthenticateUser u = AuthenticateUser.findByAuthUserIdentity(knownUser);
        u.lastLogin = new Date();
        u.save();
    }

    public static AuthenticateUser findByEmail(final String email) {
        return getEmailUserFind(email).findUnique();
    }

    private static ExpressionList<AuthenticateUser> getEmailUserFind(final String email) {
        return find.where().eq("active", true).eq("email", email);
    }

    public AuthenticateLinkedAccount getAccountByProvider(final String providerKey) {
        return AuthenticateLinkedAccount.findByProviderKey(this, providerKey);
    }

    public static void verify(final AuthenticateUser unverified) {
        // You might want to wrap this into a transaction
        unverified.emailValidated = true;
        unverified.save();
        AuthenticateTokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
    }

    public void changePassword(final UsernamePasswordAuthUser authUser,
                               final boolean create) {
        AuthenticateLinkedAccount a = this.getAccountByProvider(authUser.getProvider());
        if (a == null) {
            if (create) {
                a = AuthenticateLinkedAccount.create(authUser);
                a.user = this;
            } else {
                throw new RuntimeException(
                        "Account not enabled for password usage");
            }
        }
        a.providerUserId = authUser.getHashedPassword();
        a.save();
    }

    public void resetPassword(final UsernamePasswordAuthUser authUser,
                              final boolean create) {
        // You might want to wrap this into a transaction
        this.changePassword(authUser, create);
        AuthenticateTokenAction.deleteByUser(this, Type.PASSWORD_RESET);
    }

    public String toString(){
        return this.name;
    }
}

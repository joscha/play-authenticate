package models;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.db.ebean.Model;
import scala.actors.threadpool.Arrays;
import be.objectify.deadbolt.models.Permission;
import be.objectify.deadbolt.models.Role;
import be.objectify.deadbolt.models.RoleHolder;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.validation.Email;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.BasicIdentity;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
public class User extends Model implements RoleHolder {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@Email
	public String email;

	public String name;
	
	public Date lastLogin;

	public boolean active;

	@ManyToMany
	public List<SecurityRole> roles;

	@OneToMany(cascade = CascadeType.ALL)
	public List<LinkedAccount> linkedAccounts;

	@ManyToMany
	public List<UserPermission> permissions;

	public static final Finder<Long, User> find = new Finder<Long, User>(
			Long.class, User.class);

	public List<? extends Role> getRoles() {
		return roles;
	}

	public List<? extends Permission> getPermissions() {
		return permissions;
	}

	public static boolean existsByAuthUserIdentity(
			final AuthUserIdentity identity) {
		return getAuthUserFind(identity).findRowCount() > 0;
	}

	private static ExpressionList<User> getAuthUserFind(
			final AuthUserIdentity identity) {
		return find.where()
				.eq("linkedAccounts.providerUserId", identity.getId())
				.eq("linkedAccounts.providerKey", identity.getProvider())
				.eq("active", true);
	}

	public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
		return getAuthUserFind(identity).findUnique();
	}

	public void merge(final User otherUser) {
		for (final LinkedAccount acc : otherUser.linkedAccounts) {
			this.linkedAccounts.add(LinkedAccount.create(acc));
		}
		// do all other mergin stuff here - like resources, etc.

		// deactivate the merged user that got added to this one
		otherUser.active = false;
		Ebean.save(Arrays.asList(new User[] { otherUser, this }));
	}

	public static User create(final AuthUser authUser) {
		final User user = new User();
		user.roles = Collections.singletonList(SecurityRole
				.findByRoleName(controllers.Application.USER_ROLE));
		// user.permissions = new ArrayList<UserPermission>();
		// user.permissions.add(UserPermission.findByValue("printers.edit"));
		user.active = true;
		user.lastLogin = new Date();
		user.linkedAccounts = Collections.singletonList(LinkedAccount
				.create(authUser));

		if (authUser instanceof BasicIdentity) {
			final BasicIdentity identity = (BasicIdentity) authUser;
			user.name = identity.getName();

			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application!
			user.email = identity.getEmail();
		}

		user.save();
		user.saveManyToManyAssociations("roles");
		// user.saveManyToManyAssociations("permissions");
		return user;
	}

	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
		User.findByAuthUserIdentity(oldUser).merge(
				User.findByAuthUserIdentity(newUser));
	}

	public static void addLinkedAccount(final AuthUser oldUser,
			final AuthUser newUser) {
		final User u = User.findByAuthUserIdentity(oldUser);
		u.linkedAccounts.add(LinkedAccount.create(newUser));
		u.save();
	}

	public static void setLastLoginDate(final AuthUser knownUser) {
		final User u = User.findByAuthUserIdentity(knownUser);
		u.lastLogin = new Date();
		u.save();
	}
}

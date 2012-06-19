/*
 * Copyright 2012 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package models;

import be.objectify.deadbolt.models.Permission;
import be.objectify.deadbolt.models.Role;
import be.objectify.deadbolt.models.RoleHolder;
import play.db.ebean.Model;
import scala.actors.threadpool.Arrays;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class User extends Model implements RoleHolder {
	@Id
	public Long id;

	public String userName;
	
	public boolean active;

	@ManyToMany
	public List<SecurityRole> roles;

	@OneToMany(cascade=CascadeType.ALL)
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

	public static User findByUserName(String userName) {
		return find.where().eq("userName", userName).findUnique();
	}
	
	public static boolean existsByAuthUserIdentity(final AuthUserIdentity identity) {
		return getAuthUserFind(identity).findRowCount() > 0;
	}

	private static ExpressionList<User> getAuthUserFind(
			final AuthUserIdentity identity) {
		return find.where().eq("linkedAccounts.providerUserId", identity.getId())
		.eq("linkedAccounts.providerKey", identity.getProvider()).eq("active", true);
	}

	public static User findByAuthUserIdentity(
			final AuthUserIdentity identity) {
		return getAuthUserFind(identity)
				.findUnique();
	}

	public void merge(final User otherUser) {
		for(final LinkedAccount acc : otherUser.linkedAccounts) {
			this.linkedAccounts.add(LinkedAccount.create(acc));
		}
		// do all other mergin stuff here - like resources, etc.
		
		// deactivate the merged user that got added to this one
		otherUser.active = false;
		Ebean.save(Arrays.asList(new User[] {otherUser,this}));
	}

	public static User create(final AuthUser authUser) {
		final User user = new User();
		//user.userName = "steve";
		user.roles = Collections.singletonList(SecurityRole.findByRoleName(controllers.Application.USER_ROLE));
		// user.permissions = new ArrayList<UserPermission>();
		// user.permissions.add(UserPermission.findByValue("printers.edit"));
		user.active = true;
		user.linkedAccounts = Collections.singletonList(LinkedAccount.create(authUser));

		user.save();
		user.saveManyToManyAssociations("roles");
		// user.saveManyToManyAssociations("permissions");
		return user;
	}

	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
		User.findByAuthUserIdentity(oldUser).merge(User.findByAuthUserIdentity(newUser));
	}

	public static void addLinkedAccount(final AuthUser oldUser, final AuthUser newUser) {
		final User u = User.findByAuthUserIdentity(oldUser);
		u.linkedAccounts.add(LinkedAccount.create(newUser));
		u.save();
	}
}

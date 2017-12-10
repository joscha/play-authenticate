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

import be.objectify.deadbolt.java.models.Role;
import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class SecurityRole extends Model implements Role {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public String roleName;

	public static final Finder<Long, SecurityRole> find = new Finder<>(SecurityRole.class);

	@Override
	public String getName() {
		return roleName;
	}

	public static SecurityRole findByRoleName(String roleName) {
		return find.query().where().eq("roleName", roleName).findUnique();
	}
}

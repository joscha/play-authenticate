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
package security;

import java.util.Collections;
import java.util.List;

import models.User;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import be.objectify.deadbolt.AbstractDeadboltHandler;
import be.objectify.deadbolt.DynamicResourceHandler;
import be.objectify.deadbolt.models.Permission;
import be.objectify.deadbolt.models.Role;
import be.objectify.deadbolt.models.RoleHolder;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.AuthUserIdentity;

public class MyDeadboltHandler extends AbstractDeadboltHandler {
	@Override
	public Result beforeRoleCheck(Http.Context context) {
		if (PlayAuthenticate.isLoggedIn(context.session())) {
			// user is logged in
			return null;
		} else {
			// user is not logged in
			
			// call this if you want to redirect your visitor to the page that was requested before sending him to the login page
			// if you don't call this, the user will get redirected to the page defined by your resolver
			final String originalUrl = PlayAuthenticate.storeOriginalUrl(context);
			
			context.flash().put("error", "You need to log in first, to view '"+originalUrl+"'");
			return redirect(PlayAuthenticate.getResolver().login());
		}
	}

	@Override
	public RoleHolder getRoleHolder(Http.Context context) {
		final AuthUserIdentity u = PlayAuthenticate.getUser(context);
		// Caching might be a good idea here
		return User.findByAuthUserIdentity(u);
	}

	@Override
	public DynamicResourceHandler getDynamicResourceHandler(Http.Context context) {
		return null;
	}

	@Override
	public Result onAccessFailure(Http.Context context, String content) {
		return forbidden();
	}
}

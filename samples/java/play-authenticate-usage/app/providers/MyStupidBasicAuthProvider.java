/*
 * Copyright © 2014 Florian Hars, nMIT Solutions GmbH
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
package providers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.wwwauth.basic.BasicAuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import play.data.Form;
import play.data.FormFactory;
import play.inject.ApplicationLifecycle;
import play.mvc.Http.Context;
import play.twirl.api.Content;
import service.UserProvider;
import views.html.login;

import javax.inject.Inject;
import javax.inject.Singleton;

/** A really simple basic auth provider that accepts one hard coded user */
@Singleton
public class MyStupidBasicAuthProvider extends BasicAuthProvider {

	private final UserProvider userProvider;

	private final Form<MyUsernamePasswordAuthProvider.MyLogin> LOGIN_FORM;

	@Inject
	public MyStupidBasicAuthProvider(final PlayAuthenticate auth, final UserProvider userProvider,
									 final FormFactory formFactory,
									 final ApplicationLifecycle lifecycle) {
		super(auth, lifecycle);
		this.userProvider = userProvider;
		this.LOGIN_FORM = formFactory.form(MyUsernamePasswordAuthProvider.MyLogin.class);
	}

	@Override
	protected AuthUser authenticateUser(String username, String password) {
		if (username.equals("example") && password.equals("secret")) {
			return new AuthUser() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getId() {
					return "example";
				}

				@Override
				public String getProvider() {
					return "basic";
				}
			};
		}
		return null;
	}

	@Override
	public String getKey() {
		return "basic";
	}

	/** Diplay the normal login form if HTTP authentication fails */
	@Override
	protected Content unauthorized(Context context) {
		return login.render(this.auth, this.userProvider, this.LOGIN_FORM);
	}
}

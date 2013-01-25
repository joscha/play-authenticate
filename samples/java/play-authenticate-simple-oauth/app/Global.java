import play.Application;
import play.GlobalSettings;
import play.mvc.Call;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;

import controllers.routes;

public class Global extends GlobalSettings {

	public void onStart(final Application app) {
		PlayAuthenticate.setResolver(new Resolver() {

			@Override
			public Call login() {
				// Your login page
				return routes.Application.index();
			}

			@Override
			public Call afterAuth() {
				// The user will be redirected to this page after authentication
				// if no original URL was saved
				return routes.Application.index();
			}

			@Override
			public Call afterLogout() {
				return routes.Application.index();
			}

			@Override
			public Call auth(final String provider) {
				// You can provide your own authentication implementation,
				// however the default should be sufficient for most cases
				return com.feth.play.module.pa.controllers.routes.Authenticate
						.authenticate(provider);
			}

			@Override
			public Call onException(final AuthException e) {
				if (e instanceof AccessDeniedException) {
					return routes.Application
							.oAuthDenied(((AccessDeniedException) e)
									.getProviderKey());
				}

				// more custom problem handling here...

				return super.onException(e);
			}

			@Override
			public Call askLink() {
				// We don't support moderated account linking in this sample.
				// See the play-authenticate-usage project for an example
				return null;
			}

			@Override
			public Call askMerge() {
				// We don't support moderated account merging in this sample.
				// See the play-authenticate-usage project for an example
				return null;
			}
		});
	}

}
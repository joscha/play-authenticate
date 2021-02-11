import java.util.Arrays;

import javax.persistence.EntityManager;

import models.SecurityRole;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;

import constants.JpaConstants;
import controllers.routes;
import dao.SecurityRoleHome;
import play.Application;
import play.GlobalSettings;
import play.db.jpa.JPA;
import play.mvc.Call;

public class Global extends GlobalSettings {

	@Override
	public void onStart(Application app) {
		PlayAuthenticate.setResolver(new Resolver() {

			@Override
			public Call login() {
				// Your login page
				return routes.Application.login();
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
			public Call auth(String provider) {
				// You can provide your own authentication implementation,
				// however the default should be sufficient for most cases
				return com.feth.play.module.pa.controllers.routes.AuthenticateDI.authenticate(provider);
			}

			@Override
			public Call askMerge() {
				return routes.Account.askMerge();
			}

			@Override
			public Call askLink() {
				return routes.Account.askLink();
			}

			@Override
			public Call onException(AuthException e) {
				if (e instanceof AccessDeniedException) {
					return routes.Signup
							.oAuthDenied(((AccessDeniedException) e)
									.getProviderKey());
				}

				// more custom problem handling here...
				return super.onException(e);
			}
		});

		initialData();
	}

	private void initialData() {
		
		EntityManager em = JPA.em(JpaConstants.DB);
		
		SecurityRoleHome dao = new SecurityRoleHome();
		
		if (!dao.hasInitialData(em)) {
			for (String roleName : Arrays
					.asList(controllers.Application.USER_ROLE)) {
				SecurityRole role = new SecurityRole();
				role.setRoleName(roleName);
				dao.persist(role, em);
			}
		}
		
		em.close();
	}
}
package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Http.Session;
import play.mvc.Result;
import views.html.index;

import com.feth.play.module.pa.PlayAuthenticate;

public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";

	public static Result index() {
		return ok(index.render());
	}

	public static Result oAuthDenied(final String providerKey) {
		flash(FLASH_ERROR_KEY,
				"You need to accept the OAuth connection in order to use this website!");
		return redirect(routes.Application.index());
	}

	public static User getLocalUser(final Session session) {
		final User localUser = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session));
		return localUser;
	}

}
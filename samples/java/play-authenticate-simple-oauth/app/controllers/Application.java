package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Http.Response;
import play.mvc.Http.Session;
import play.mvc.Result;
import views.html.index;

import com.feth.play.module.pa.PlayAuthenticate;

public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";
	
	public static void noCache() {
		// http://stackoverflow.com/questions/49547/making-sure-a-web-page-is-not-cached-across-all-browsers
		response().setHeader(Response.CACHE_CONTROL, "no-cache, no-store, must-revalidate");  // HTTP 1.1
		response().setHeader(Response.PRAGMA, "no-cache");  // HTTP 1.0.
		response().setHeader(Response.EXPIRES, "0");  // Proxies.
	}

	public static Result index() {
		noCache();
		return ok(index.render());
	}

	public static Result oAuthDenied(final String providerKey) {
		noCache();
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
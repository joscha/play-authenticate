package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import javax.inject.Inject;

public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";

	private final PlayAuthenticate auth;

	@Inject
	public Application(final PlayAuthenticate auth) {
		this.auth = auth;
	}

	public Result index() {
		return ok(index.render(this.auth));
	}

	public Result oAuthDenied(final String providerKey) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		flash(FLASH_ERROR_KEY,
				"You need to accept the OAuth connection in order to use this website!");
		return redirect(routes.Application.index());
	}
}
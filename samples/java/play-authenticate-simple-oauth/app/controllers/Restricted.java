package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import service.UserService;
import views.html.restricted;

import javax.inject.Inject;

@Security.Authenticated(Secured.class)
public class Restricted extends Controller {

	private final PlayAuthenticate auth;

	private final UserService userService;

	@Inject
	public Restricted(final PlayAuthenticate auth, final UserService userService) {
		this.auth = auth;
		this.userService = userService;
	}

	public Result index() {
		final User localUser = this.userService.getLocalUser(this.auth.getUser(session()));
		return ok(restricted.render(this.auth, localUser));
	}
}

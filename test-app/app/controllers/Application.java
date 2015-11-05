package controllers;

import static play.data.Form.form;

import com.feth.play.module.pa.PlayAuthenticate;

import play.Logger;
import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import providers.TestUsernamePasswordAuthProvider;
import providers.TestUsernamePasswordAuthProvider.Login;
import providers.TestUsernamePasswordAuthProvider.Signup;

public class Application extends Controller {

	public static final String FLASH_ERROR_KEY = "error";

	public Result index() {
		return ok(views.html.index.render());
	}

	public Result login() {
		return ok(views.html.login.render(form(Login.class).bindFromRequest()));
	}

	public Result doLogin() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<Login> filledForm = form(Login.class).bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(views.html.login.render(filledForm));
		} else {
			// Everything was filled
			return TestUsernamePasswordAuthProvider.handleLogin(ctx());
		}
	}

	public Result signup() {
		return ok(views.html.signup
				.render(form(Signup.class).bindFromRequest()));
	}

	public Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<Signup> filledForm = form(Signup.class).bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(views.html.signup.render(filledForm));
		} else {
			// Everything was filled
			return TestUsernamePasswordAuthProvider.handleSignup(ctx());
		}
	}

	public Result userExists() {
		return badRequest("User exists.");
	}

	public Result userUnverified() {
		return badRequest("User not yet verified.");
	}

	public Result verify(String token) {
		TestUsernamePasswordAuthProvider.LoginUser loginUser = upAuthProvider()
				.verifyWithToken(token);
		if (loginUser == null) {
			return notFound();
		}
		return PlayAuthenticate.loginAndRedirect(ctx(), loginUser);
	}

	public Result oAuthDenied(String providerKey) {
		flash(FLASH_ERROR_KEY, "You need to accept the OAuth connection"
				+ " in order to use this website!");
		return redirect(routes.Application.index());
	}

	private static TestUsernamePasswordAuthProvider upAuthProvider() {
		return Play.application()
				.plugin(TestUsernamePasswordAuthProvider.class);
	}

}

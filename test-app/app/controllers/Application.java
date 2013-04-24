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

	public static Result index() {
		return ok(views.html.index.render());
	}

	public static Result login() {
		return ok(views.html.login.render(form(Login.class).bindFromRequest()));
	}

	public static Result doLogin() {
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

	public static Result signup() {
		return ok(views.html.signup
				.render(form(Signup.class).bindFromRequest()));
	}

	public static Result doSignup() {
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

	public static Result userExists() {
		return badRequest("User exists.");
	}

	public static Result userUnverified() {
		return badRequest("User not yet verified.");
	}

	public static Result verify(String token) {
		TestUsernamePasswordAuthProvider.LoginUser loginUser = upAuthProvider()
				.verifyWithToken(token);
		if (loginUser == null) {
			return notFound();
		}
		return PlayAuthenticate.loginAndRedirect(ctx(), loginUser);
	}

	public static Result oAuthDenied(String providerKey) {
		flash(FLASH_ERROR_KEY, "You need to accept the OAuth connection"
				+ " in order to use this website!");
		return redirect(routes.Application.index());
	}

	private static TestUsernamePasswordAuthProvider upAuthProvider() {
		return Play.application()
				.plugin(TestUsernamePasswordAuthProvider.class);
	}

}

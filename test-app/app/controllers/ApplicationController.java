package controllers;

import com.feth.play.module.pa.PlayAuthenticate;

import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import providers.TestUsernamePasswordAuthProvider;
import providers.TestUsernamePasswordAuthProvider.Login;
import providers.TestUsernamePasswordAuthProvider.Signup;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ApplicationController extends Controller {

	@Inject FormFactory formFactory;

	public final String FLASH_ERROR_KEY = "error";

	private TestUsernamePasswordAuthProvider testProvider;

	@Inject
	public ApplicationController(TestUsernamePasswordAuthProvider testProvider) {
		this.testProvider = testProvider;
	}

	public Result index() {
		return ok(views.html.index.render(testProvider.getAuth()));
	}

	public Result login() {
		return ok(views.html.login.render(formFactory.form(Login.class).bindFromRequest()));
	}

	public Result doLogin() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<Login> filledForm = formFactory.form(Login.class).bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(views.html.login.render(filledForm));
		} else {
			// Everything was filled
			return testProvider.handleLogin(ctx());
		}
	}

	public Result signup() {
		return ok(views.html.signup
				.render(formFactory.form(Signup.class).bindFromRequest()));
	}

	public Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<Signup> filledForm = formFactory.form(Signup.class).bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(views.html.signup.render(filledForm));
		} else {
			// Everything was filled
			return testProvider.handleSignup(ctx());
		}
	}

	public Result userExists() {
		return badRequest("User exists.");
	}

	public Result userUnverified() {
		return badRequest("User not yet verified.");
	}

	public Result verify(String token) {
		TestUsernamePasswordAuthProvider.LoginUser loginUser = this.testProvider
				.verifyWithToken(token);
		if (loginUser == null) {
			return notFound();
		}
		return testProvider.getAuth().loginAndRedirect(ctx(), loginUser);
	}

	public Result oAuthDenied(String providerKey) {
		flash(FLASH_ERROR_KEY, "You need to accept the OAuth connection"
				+ " in order to use this website!");
		return redirect(routes.ApplicationController.index());
	}

}

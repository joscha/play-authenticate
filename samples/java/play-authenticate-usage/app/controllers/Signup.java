package controllers;

import java.util.Date;

import models.User;
import models.UserActivation;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.account.signup.*;


public class Signup extends Controller {

	public static Result unverified() {
		return ok(unverified.render());
	}
	
	public static Result exists() {
		return ok(exists.render());
	}
	
	public static Result verify(final String token) {
		if(token == null || token.trim().isEmpty()) {
			return badRequest(verify_no_token.render());
		}
		
		final UserActivation ua = UserActivation.findByToken(token);
		if(ua == null || ua.expires.before(new Date())) {
			return badRequest(verify_no_token.render());
		}
		
		User.verify(ua.unverified);
		flash("message","Email address successfully verified");
		
		return redirect(routes.Application.login());
	}
}

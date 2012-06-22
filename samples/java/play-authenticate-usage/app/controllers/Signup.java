package controllers;

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
}

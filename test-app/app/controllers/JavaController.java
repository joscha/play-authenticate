package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import controllers.routes;

public class JavaController extends Controller {

	public static final String FLASH_ERROR_KEY = "error";

	@Security.Authenticated(JavaSecured.class)
	public static Result index() {
		AuthUser user = PlayAuthenticate.getUser(ctx());
		return ok(user.getProvider() + ": " + user.getId());
	}

}

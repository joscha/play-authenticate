package controllers;

import play.mvc.*;

import views.html.*;

import be.objectify.deadbolt.actions.Restrict;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.AuthProvider.Registry;

public class Application extends Controller {
  
  public static final String USER_ROLE = "user";
  
  public static Result index() {
    return ok(index.render());
  }
  
  @Restrict("user")
  public static Result restricted() {
	  return ok(restricted.render());
  }
  
  public static Result login() {
	return ok(login.render(Registry.getProviders()));
  }
  
  public static Result logout() {
	  PlayAuthenticate.logout(session());
	  return redirect(routes.Application.index());
  }
  
}
package controllers;

import be.objectify.deadbolt.actions.Restrict;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import models.AuthenticateUser;
import play.Routes;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.Session;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;
import views.html.authenticate.profile;
import views.html.authenticate.restricted;
import views.html.authenticate.signup;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Authenticate extends Controller {

    public static final String FLASH_MESSAGE_KEY = "message";
    public static final String FLASH_ERROR_KEY = "error";
    public static final String USER_ROLE = "user";

    public static Result index() {
        return ok(views.html.authenticate.index.render());
    }

    public static Result redirectToIndex(){
        return redirect(routes.Authenticate.index());
    }

    public static AuthenticateUser getLocalUser(final Session session) {
        final AuthenticateUser localUser = AuthenticateUser.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session));
        return localUser;
    }

    @Restrict(Authenticate.USER_ROLE)
    public static Result restricted() {
        final AuthenticateUser localUser = getLocalUser(session());
        return ok(restricted.render(localUser));
    }

    @Restrict(Authenticate.USER_ROLE)
    public static Result profile() {
        final AuthenticateUser localUser = getLocalUser(session());
        return ok(profile.render(localUser));
    }

    public static Result login() {
        return ok(views.html.authenticate.login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM));
    }

    public static Result doLogin() {
        final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
                .bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not fill everything properly
            return badRequest(views.html.authenticate.login.render(filledForm));
        } else {
            // Everything was filled
            return UsernamePasswordAuthProvider.handleLogin(ctx());
        }
    }

    public static Result signup() {
        return ok(signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
    }

    public static Result jsRoutes() {
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        controllers.routes.javascript.AuthenticateSignup.forgotPassword()))
                .as("text/javascript");
    }

    public static Result doSignup() {
        final Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
                .bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not fill everything properly
            return badRequest(signup.render(filledForm));
        } else {
            // Everything was filled
            // do something with your part of the form before handling the user
            // signup
            return UsernamePasswordAuthProvider.handleSignup(ctx());
        }
    }

    public static String formatTimestamp(final long t) {
        return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
    }

}
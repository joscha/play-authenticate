package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import play.libs.ws.*;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;

import models.User;
import play.Routes;
import play.data.Form;
import play.mvc.*;
import play.mvc.Http.Response;
import play.mvc.Http.Session;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import views.html.*;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.user.AuthUser;

public class Application extends Controller {

    public static final String FLASH_MESSAGE_KEY = "message";
    public static final String FLASH_ERROR_KEY = "error";
    public static final String USER_ROLE = "user";

    public static Result index() {
        return ok(index.render());
    }

    public static User getLocalUser(final Session session) {
        final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
        final User localUser = User.findByAuthUserIdentity(currentAuthUser);
        return localUser;
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result restricted() {
        final User localUser = getLocalUser(session());
        return ok(restricted.render(localUser));
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result profile() {
        final User localUser = getLocalUser(session());
        return ok(profile.render(localUser));
    }

    public static Result login() {
        return ok(login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM));
    }

    public static Result doLogin() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
                .bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not fill everything properly
            return badRequest(login.render(filledForm));
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
                        controllers.routes.javascript.Signup.forgotPassword()))
                .as("text/javascript");
    }

    public static String captchaResp(String gcaptchaCode) {
        String googUrl = "https://www.google.com/recaptcha/api/siteverify";
        String encSecret = "";
        String encCapcode = "";
        String error = "-1";
        URL url = null;
        // Get the secret key
        Config conf = ConfigFactory.load();
        String gsecretKey = conf.getString("play-authenticate.gcaptcha.gsecretKey");
        // Debug -- show values on console
        // System.out.println("gsecretKey = " + gsecretKey);
        // System.out.println("captchacode = " + gcaptchaCode);
        try {
            encSecret = URLEncoder.encode(gsecretKey, "UTF-8");
            encCapcode = URLEncoder.encode(gcaptchaCode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return error;
        }
        String query = "secret=" + encSecret + "&response=" + encCapcode;
        try {
            url = new URL(googUrl + "?" + query);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return error;
        }
        StringBuilder stringBuilder = new StringBuilder();
        try {
            // Check if Google validates the captcha response
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            // 10 seconds max to respond
            connection.setReadTimeout(10 * 1000);
            connection.connect();
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + connection.getResponseCode());
            }
            // read the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return error;
    }

    public static Result doSignup() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
                .bindFromRequest();

        final Map<String, String[]> values = request().body().asFormUrlEncoded();
        final String gcaptchaCode = values.get("g-recaptcha-response")[0];
        String error = "-1";

        if (filledForm.hasErrors()) {
            // User did not fill everything properly
            return badRequest(signup.render(filledForm));
        } else {
            // Everything was filled
            // do something with your part of the form before handling the user
            // signup
            //
            // Check if captcha was filled in
            if (gcaptchaCode == null || gcaptchaCode.isEmpty()) {
                flash("error", "You need to successfully solve the reCAPTCHA at the bottom of the form in order to signup.");
                return badRequest(signup.render(filledForm));
            }

            // Find out if Google likes the Captcha
            String json = captchaResp(gcaptchaCode);

            // Check if an error occured while contacting Google and processing
            if (json.equals(error)) {
                flash("error", "An error occured while attempting to resolve the Google Captcha. Try again?");
                return badRequest(signup.render(filledForm));
            }

            // Turn the json string into a Json object
            JsonNode jobj = Json.parse(json);
            Boolean captchaPassed = jobj.findPath("success").booleanValue();

            if (captchaPassed) {
                return UsernamePasswordAuthProvider.handleSignup(ctx());
            } else {
                // Error codes are in jobj.findPath("error-codes").textValue(); 
                flash("error", "You need to successfully solve the reCAPTCHA at the bottom of the form in order to signup.");
                return badRequest(signup.render(filledForm));
            }
        }
    }

    public static String formatTimestamp(final long t) {
        return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
    }

}
package providers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import models.LinkedAccount;
import models.User;
import models.UserActivation;
import play.Application;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Http.Context;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.Mailer.Mail.Body;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;

import controllers.routes;

public class MyUsernamePasswordAuthProvider
		extends
		UsernamePasswordAuthProvider<String, MyLoginUsernamePasswordAuthUser, MyUsernamePasswordAuthUser, MyUsernamePasswordAuthProvider.MyLogin, MyUsernamePasswordAuthProvider.MySignup> {

	private static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL
			+ "." + "verificationLink.secure";

	@Override
	protected List<String> neededSettingKeys() {
		final List<String> needed = new ArrayList<String>(
				super.neededSettingKeys());
		needed.add(SETTING_KEY_VERIFICATION_LINK_SECURE);
		return needed;
	}

	public static class MyLogin
			implements
			com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.UsernamePassword {

		@Required
		@Email
		public String email;

		@Required
		@MinLength(5)
		public String password;

		@Override
		public String getEmail() {
			return email;
		}

		@Override
		public String getPassword() {
			return password;
		}
	}

	public static class MySignup extends MyLogin {
		
		@Required
		@MinLength(5)
		public String repeatPassword;
		
		@Required
		public String name;

		public String validate() {
			if (!password.equals(repeatPassword)) {
				return "Passwords don't match!";
			}
			return null;
		}
	}

	public static Form<MySignup> SIGNUP_FORM = Controller.form(MySignup.class);
	public static Form<MyLogin> LOGIN_FORM = Controller.form(MyLogin.class);

	public MyUsernamePasswordAuthProvider(Application app) {
		super(app);
	}

	protected Form<MySignup> getSignupForm() {
		return SIGNUP_FORM;
	}

	protected Form<MyLogin> getLoginForm() {
		return LOGIN_FORM;
	}

	@Override
	protected SignupResult signupUser(final MyUsernamePasswordAuthUser user) {
		final User u = User.findByEmail(user.getEmail());
		if (u != null) {
			if (u.emailValidated) {
				// This user exists, has its email validated and is active
				return SignupResult.USER_EXISTS;
			} else {
				// this user exists, is active but has not yet validated its
				// email
				return SignupResult.USER_EXISTS_UNVERIFIED;
			}
		}
		// The user either does not exist or is inactive - create a new one
		@SuppressWarnings("unused")
		final User newUser = User.create(user);
		// Usually the email should be verified before allowing login, however
		// if you return
		// return SignupResult.USER_CREATED;
		// then the user gets logged in directly
		return SignupResult.USER_CREATED_UNVERIFIED;
	}

	@Override
	protected LoginResult loginUser(
			final MyLoginUsernamePasswordAuthUser authUser) {
		final User u = User.findByUsernamePasswordIdentity(authUser);
		if (u == null) {
			return LoginResult.NOT_FOUND;
		} else {
			if (!u.emailValidated) {
				return LoginResult.USER_UNVERIFIED;
			} else {
				for (final LinkedAccount acc : u.linkedAccounts) {
					if (getKey().equals(acc.providerKey)) {
						if (authUser.checkPassword(acc.providerUserId,
								authUser.getPassword())) {
							// Password was correct
							return LoginResult.USER_LOGGED_IN;
						} else {
							// if you don't return here,
							// you would allow the user to have
							// multiple passwords defined
							// usually we don't want this
							return LoginResult.WRONG_PASSWORD;
						}
					}
				}
				return LoginResult.WRONG_PASSWORD;
			}
		}
	}

	@Override
	protected Call userExists(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.exists();
	}

	@Override
	protected Call userUnverified(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.unverified();
	}

	@Override
	protected MyUsernamePasswordAuthUser buildSignupAuthUser(
			final MySignup signup) {
		return new MyUsernamePasswordAuthUser(signup);
	}

	@Override
	protected MyLoginUsernamePasswordAuthUser buildLoginAuthUser(MyLogin login) {
		return new MyLoginUsernamePasswordAuthUser(login.getPassword(),
				login.getEmail());
	}

	@Override
	protected String getVerifyEmailMailingSubject(
			final MyUsernamePasswordAuthUser user, final Context ctx) {
		return Messages.get("password.verify_email.subject");
	}

	@Override
	protected String onLoginUserNotFound(final Context context) {
		context.flash().put("error",
				"User could not be found or password was wrong.");
		return super.onLoginUserNotFound(context);
	}

	@Override
	protected Body getVerifyEmailMailingBody(final String token,
			final MyUsernamePasswordAuthUser user, final Context ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);

		final String url = routes.Signup.verify(token).absoluteURL(
				ctx.request(), isSecure);

		final String html = views.html.account.signup.verify_email_body.render(
				url, token, user.getName()).toString();
		final String text = views.txt.account.signup.verify_email_body.render(
				url, token, user.getName()).toString();
		return new Body(text, html);
	}

	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds
	 * Defaults to one week
	 */
	final static long VERIFICATION_TIME = 7 * 24 * 3600;

	@Override
	protected String generateVerificationRecord(
			final MyUsernamePasswordAuthUser user) {
		final String token = UUID.randomUUID().toString();
		// Do database actions, etc.
		final UserActivation ua = new UserActivation();
		ua.unverified = User.findByAuthUserIdentity(user);
		ua.token = token;
		final Date expirationDate = new Date();
		expirationDate.setTime(System.currentTimeMillis() + VERIFICATION_TIME
				* 1000);
		ua.expires = expirationDate;
		ua.save();
		return token;
	}
}

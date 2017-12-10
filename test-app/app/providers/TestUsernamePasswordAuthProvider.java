package providers;

import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.mail.Mailer.MailerFactory;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.inject.ApplicationLifecycle;
import play.mvc.Call;
import play.mvc.Http.Context;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

@Singleton
public class TestUsernamePasswordAuthProvider
		extends
		UsernamePasswordAuthProvider<String, TestUsernamePasswordAuthProvider.LoginUser, TestUsernamePasswordAuthProvider.SignupUser, TestUsernamePasswordAuthProvider.Login, TestUsernamePasswordAuthProvider.Signup> {

	private final Map<String, String> verifiedUsers = new HashMap<>();
	private final Map<String, String> unverifiedUsers = new HashMap<>();
	private final Map<String, String> verificationTokens = new HashMap<>();
	private final FormFactory formFactory;

	@Inject
	public TestUsernamePasswordAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final MailerFactory mailerFactory, final FormFactory formFactory) {
		super(auth, lifecycle, mailerFactory);
		this.formFactory = formFactory;
	}

	public static class Login implements
			UsernamePasswordAuthProvider.UsernamePassword {

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

	public static class Signup extends Login {
	}

	public static class SignupUser extends UsernamePasswordAuthUser {
		private static final long serialVersionUID = 1L;

		public SignupUser(final String clearPassword, final String email) {
			super(clearPassword, email);
		}
	}

	public static class LoginUser extends UsernamePasswordAuthUser {
		private static final long serialVersionUID = 1L;

		public LoginUser(final String email) {
			super(null, email);
		}

		public LoginUser(final String clearPassword, final String email) {
			super(clearPassword, email);
		}
	}

	// Only used for testing.
	public String getVerificationToken(String email) {
		for (Entry<String, String> e : verificationTokens.entrySet()) {
			if (e.getValue().equals(email)) {
				return e.getKey();
			}
		}
		return null;
	}

	public LoginUser verifyWithToken(String token) {
		if (verificationTokens.containsKey(token)) {
			final String email = verificationTokens.get(token);
			if (!unverifiedUsers.containsKey(email)) {
				return null;
			}
			final String hashedPassword = unverifiedUsers.get(email);
			verifiedUsers.put(email, hashedPassword);
			verificationTokens.remove(token);
			unverifiedUsers.remove(email);
			return new LoginUser(email);
		} else {
			return null;
		}
	}

	@Override
	protected String generateVerificationRecord(SignupUser user) {
		final String token = UUID.randomUUID().toString();
		verificationTokens.put(token, user.getEmail());
		return token;
	}

	@Override
	protected String getVerifyEmailMailingSubject(SignupUser user, Context ctx) {
		return "Please verify your email address";
	}

	@Override
	protected Body getVerifyEmailMailingBody(String verificationRecord,
			SignupUser user, Context ctx) {
		// No human will ever look at this body, so make it simple
		return new Body(verificationRecord);
	}

	@Override
	protected LoginUser buildLoginAuthUser(Login login, Context ctx) {
		return new LoginUser(login.getPassword(), login.getEmail());
	}

	@Override
	protected LoginUser transformAuthUser(SignupUser signupUser, Context context) {
		return new LoginUser(signupUser.getEmail());
	}

	@Override
	protected SignupUser buildSignupAuthUser(Signup signup, Context ctx) {
		return new SignupUser(signup.getPassword(), signup.getEmail());
	}

	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.LoginResult loginUser(
			LoginUser user) {
		final String e = user.getEmail();
		if (unverifiedUsers.containsKey(e)) {
			Logger.debug(e + " attempted to login but is still unverified.");
			return LoginResult.USER_UNVERIFIED;
		}
		if (!verifiedUsers.containsKey(e)) {
			Logger.debug(e + " attempted to login but was not found.");
			return LoginResult.NOT_FOUND;
		}
		final boolean passwordCorrect = user.checkPassword(
				verifiedUsers.get(e), user.getPassword());
		if (!passwordCorrect) {
			Logger.debug(e + " provided an incorrect password.");
			return LoginResult.WRONG_PASSWORD;
		}
		Logger.debug(e + " successfully authenticated.");
		return LoginResult.USER_LOGGED_IN;
	}

	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.SignupResult signupUser(
			SignupUser user) {
		final String e = user.getEmail();
		if (verifiedUsers.containsKey(e)) {
			return SignupResult.USER_EXISTS;
		}
		if (unverifiedUsers.containsKey(e)) {
			return SignupResult.USER_EXISTS_UNVERIFIED;
		}
		unverifiedUsers.put(user.getEmail(), user.getHashedPassword());
		return SignupResult.USER_CREATED_UNVERIFIED;
	}

	protected Form<Signup> getSignupForm() {
		return formFactory.form(Signup.class);
	}

	protected Form<Login> getLoginForm() {
		return formFactory.form(Login.class);
	}

	@Override
	protected Signup getSignup(final Context ctx) {
		Context.current.set(ctx);
		final Form<Signup> filledForm = getSignupForm().bindFromRequest();
		return filledForm.get();
	}

	@Override
	protected Login getLogin(final Context ctx) {
		// TODO change to getLoginForm().bindFromRequest(request) after 2.1
		Context.current.set(ctx);
		final Form<Login> filledForm = getLoginForm().bindFromRequest();
		return filledForm.get();
	}

	@Override
	protected Call userExists(UsernamePasswordAuthUser authUser) {
		return controllers.routes.ApplicationController.userExists();
	}

	@Override
	protected Call userUnverified(UsernamePasswordAuthUser authUser) {
		return controllers.routes.ApplicationController.userUnverified();
	}

}

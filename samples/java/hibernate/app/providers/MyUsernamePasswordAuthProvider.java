package providers;

import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.google.inject.Inject;

import constants.JpaConstants;
import controllers.routes;
import dao.TokenActionHome;
import dao.UserHome;
import models.LinkedAccount;
import models.TokenAction;
import models.User;
import play.Application;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Call;
import play.mvc.Http.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import static play.data.Form.form;

public class MyUsernamePasswordAuthProvider
		extends
		UsernamePasswordAuthProvider<String, MyLoginUsernamePasswordAuthUser, MyUsernamePasswordAuthUser, MyUsernamePasswordAuthProvider.MyLogin, MyUsernamePasswordAuthProvider.MySignup> {

	private static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL
			+ "." + "verificationLink.secure";
	private static final String SETTING_KEY_PASSWORD_RESET_LINK_SECURE = SETTING_KEY_MAIL
			+ "." + "passwordResetLink.secure";
	private static final String SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET = "loginAfterPasswordReset";

	private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";

	@Override
	protected List<String> neededSettingKeys() {
		List<String> needed = new ArrayList<String>(
				super.neededSettingKeys());
		needed.add(SETTING_KEY_VERIFICATION_LINK_SECURE);
		needed.add(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		needed.add(SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
		return needed;
	}

	public static MyUsernamePasswordAuthProvider getProvider() {
		return (MyUsernamePasswordAuthProvider) PlayAuthenticate
				.getProvider(UsernamePasswordAuthProvider.PROVIDER_KEY);
	}

	public static class MyIdentity {

		public MyIdentity() {
		}

		public MyIdentity(String email) {
			this.email = email;
		}

		@Required
		@Email
		public String email;

	}

	public static class MyLogin extends MyIdentity
			implements
			com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.UsernamePassword {

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
			if (password == null || !password.equals(repeatPassword)) {
				return Messages
						.get("playauthenticate.password.signup.error.passwords_not_same");
			}
			return null;
		}
	}

	public static final Form<MySignup> SIGNUP_FORM = form(MySignup.class);
	public static final Form<MyLogin> LOGIN_FORM = form(MyLogin.class);

	@Inject
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
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.SignupResult signupUser(MyUsernamePasswordAuthUser user) {
		
		EntityManager em = JPA.em(JpaConstants.DB);
		
		UserHome userDao = new UserHome();
		
		User u = userDao.findByUsernamePasswordIdentity(user, em);
		if (u != null) {
			if (u.getEmailValidated()) {
				// This user exists, has its email validated and is active
				em.close();
				return SignupResult.USER_EXISTS;
			} else {
				// this user exists, is active but has not yet validated its
				// email
				em.close();
				return SignupResult.USER_EXISTS_UNVERIFIED;
			}
		}
		// The user either does not exist or is inactive - create a new one
		@SuppressWarnings("unused")
		User newUser = userDao.create(user, em);
		// Usually the email should be verified before allowing login, however
		// if you return
		// return SignupResult.USER_CREATED;
		// then the user gets logged in directly
		
		em.close();
		return SignupResult.USER_CREATED_UNVERIFIED;
	}

	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.LoginResult loginUser(MyLoginUsernamePasswordAuthUser authUser) {
		
		EntityManager em = JPA.em(JpaConstants.DB);
		
		UserHome userDao = new UserHome();
		
		User u = userDao.findByUsernamePasswordIdentity(authUser, em);
		if (u == null) {
			return LoginResult.NOT_FOUND;
		} else {
			if (!u.getEmailValidated()) {
				em.close();
				return LoginResult.USER_UNVERIFIED;
			} else {
				for (LinkedAccount acc : u.getLinkedAccounts()) {
					if (getKey().equals(acc.getProviderKey())) {
						if (authUser.checkPassword(acc.getProviderUserId(),
								authUser.getPassword())) {
							// Password was correct
							em.close();
							return LoginResult.USER_LOGGED_IN;
						} else {
							// if you don't return here,
							// you would allow the user to have
							// multiple passwords defined
							// usually we don't want this
							em.close();
							return LoginResult.WRONG_PASSWORD;
						}
					}
				}
				em.close();
				return LoginResult.WRONG_PASSWORD;
			}
		}
	}

	@Override
	protected Call userExists(UsernamePasswordAuthUser authUser) {
		return routes.Signup.exists();
	}

	@Override
	protected Call userUnverified(UsernamePasswordAuthUser authUser) {
		return routes.Signup.unverified();
	}

	@Override
	protected MyUsernamePasswordAuthUser buildSignupAuthUser(MySignup signup, Context ctx) {
		return new MyUsernamePasswordAuthUser(signup);
	}

	@Override
	protected MyLoginUsernamePasswordAuthUser buildLoginAuthUser(MyLogin login, Context ctx) {
		return new MyLoginUsernamePasswordAuthUser(login.getPassword(),
				login.getEmail());
	}
	

	@Override
	protected MyLoginUsernamePasswordAuthUser transformAuthUser(MyUsernamePasswordAuthUser authUser, Context context) {
		return new MyLoginUsernamePasswordAuthUser(authUser.getEmail());
	}

	@Override
	protected String getVerifyEmailMailingSubject(MyUsernamePasswordAuthUser user, Context ctx) {
		return Messages.get("playauthenticate.password.verify_signup.subject");
	}

	@Override
	protected String onLoginUserNotFound(Context context) {
		context.flash()
				.put(controllers.Application.FLASH_ERROR_KEY,
						Messages.get("playauthenticate.password.login.unknown_user_or_pw"));
		return super.onLoginUserNotFound(context);
	}

	@Override
	protected Body getVerifyEmailMailingBody(String token,MyUsernamePasswordAuthUser user, Context ctx) {

		boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		String url = routes.Signup.verify(token).absoluteURL(
				ctx.request(), isSecure);

		Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		String langCode = lang.code();

		String html = getEmailTemplate(
				"views.html.account.signup.email.verify_email", langCode, url,
				token, user.getName(), user.getEmail());
		String text = getEmailTemplate(
				"views.txt.account.signup.email.verify_email", langCode, url,
				token, user.getName(), user.getEmail());

		return new Body(text, html);
	}

	private static String generateToken() {
		return UUID.randomUUID().toString();
	}

	@Override
	protected String generateVerificationRecord(MyUsernamePasswordAuthUser user) {
		EntityManager em = JPA.em(JpaConstants.DB);
		
		UserHome userDao = new UserHome();
		
		String verf = generateVerificationRecord(userDao.findByAuthUserIdentity(user, em));
		
		em.close();
		return verf;
	}

	protected String generateVerificationRecord(User user) {
		EntityManager em = JPA.em(JpaConstants.DB);
		
		String token = generateToken();
		// Do database actions, etc.
		TokenActionHome tokenDao = new TokenActionHome();
		
		tokenDao.create("EMAIL_VERIFICATION", token, user, em);
		
		em.close();
		return token;
	}

	protected String generatePasswordResetRecord(User u) {
		EntityManager em = JPA.em(JpaConstants.DB);
		
		String token = generateToken();
		
		TokenActionHome tokenDao = new TokenActionHome();
		
		tokenDao.create("PASSWORD_RESET", token, u, em);
		
		em.close();
		return token;
	}

	protected String getPasswordResetMailingSubject(User user, Context ctx) {
		return Messages.get("playauthenticate.password.reset_email.subject");
	}

	protected Body getPasswordResetMailingBody(String token, User user, Context ctx) {

		boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		String url = routes.Signup.resetPassword(token).absoluteURL(
				ctx.request(), isSecure);

		Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		String langCode = lang.code();

		String html = getEmailTemplate(
				"views.html.account.email.password_reset", langCode, url,
				token, user.getName(), user.getEmail());
		String text = getEmailTemplate(
				"views.txt.account.email.password_reset", langCode, url, token,
				user.getName(), user.getEmail());

		return new Body(text, html);
	}

	public void sendPasswordResetMailing(User user, Context ctx) {
		String token = generatePasswordResetRecord(user);
		String subject = getPasswordResetMailingSubject(user, ctx);
		Body body = getPasswordResetMailingBody(token, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}

	public boolean isLoginAfterPasswordReset() {
		return getConfiguration().getBoolean(
				SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
	}

	protected String getVerifyEmailMailingSubjectAfterSignup(User user,
			Context ctx) {
		return Messages.get("playauthenticate.password.verify_email.subject");
	}

	protected String getEmailTemplate(String template,
			String langCode, String url, String token,
			String name, String email) {
		Class<?> cls = null;
		String ret = null;
		try {
			cls = Class.forName(template + "_" + langCode);
		} catch (ClassNotFoundException e) {
			Logger.warn("Template: '"
					+ template
					+ "_"
					+ langCode
					+ "' was not found! Trying to use English fallback template instead.");
		}
		if (cls == null) {
			try {
				cls = Class.forName(template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE);
			} catch (ClassNotFoundException e) {
				Logger.error("Fallback template: '" + template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE
						+ "' was not found either!");
			}
		}
		if (cls != null) {
			Method htmlRender = null;
			try {
				htmlRender = cls.getMethod("render", String.class,
						String.class, String.class, String.class);
				ret = htmlRender.invoke(null, url, token, name, email)
						.toString();

			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	protected Body getVerifyEmailMailingBodyAfterSignup(String token,
			User user, Context ctx) {

		boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		String url = routes.Signup.verify(token).absoluteURL(
				ctx.request(), isSecure);

		Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		String langCode = lang.code();

		String html = getEmailTemplate(
				"views.html.account.email.verify_email", langCode, url, token,
				user.getName(), user.getEmail());
		String text = getEmailTemplate(
				"views.txt.account.email.verify_email", langCode, url, token,
				user.getName(), user.getEmail());

		return new Body(text, html);
	}

	public void sendVerifyEmailMailingAfterSignup(User user, Context ctx) {

		String subject = getVerifyEmailMailingSubjectAfterSignup(user, ctx);
		String token = generateVerificationRecord(user);
		Body body = getVerifyEmailMailingBodyAfterSignup(token, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}

	private String getEmailName(User user) {
		return getEmailName(user.getEmail(), user.getName());
	}
}

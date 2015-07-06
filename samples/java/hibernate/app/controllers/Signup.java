package controllers;

import javax.persistence.EntityManager;

import models.TokenAction;
import models.User;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import providers.MyLoginUsernamePasswordAuthUser;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyIdentity;
import providers.MyUsernamePasswordAuthUser;
import views.html.account.signup.*;

import com.feth.play.module.pa.PlayAuthenticate;

import constants.JpaConstants;
import dao.TokenActionHome;
import dao.UserHome;
import static play.data.Form.form;

public class Signup extends Controller {

	public static class PasswordReset extends Account.PasswordChange {

		public PasswordReset() {
		}

		public PasswordReset(String token) {
			this.token = token;
		}

		public String token;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
	}

	private static final Form<PasswordReset> PASSWORD_RESET_FORM = form(PasswordReset.class);

	public Result unverified() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(unverified.render());
	}

	private static final Form<MyIdentity> FORGOT_PASSWORD_FORM = form(MyIdentity.class);

	public Result forgotPassword(String email) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		Form<MyIdentity> form = FORGOT_PASSWORD_FORM;
		if (email != null && !email.trim().isEmpty()) {
			form = FORGOT_PASSWORD_FORM.fill(new MyIdentity(email));
		}
		return ok(password_forgot.render(form));
	}

	@Transactional
	public Result doForgotPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		Form<MyIdentity> filledForm = FORGOT_PASSWORD_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill in his/her email
			return badRequest(password_forgot.render(filledForm));
		} else {
			// The email address given *BY AN UNKNWON PERSON* to the form - we
			// should find out if we actually have a user with this email
			// address and whether password login is enabled for him/her. Also
			// only send if the email address of the user has been verified.
			String email = filledForm.get().email;

			// We don't want to expose whether a given email address is signed
			// up, so just say an email has been sent, even though it might not
			// be true - that's protecting our user privacy.
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get(
							"playauthenticate.reset_password.message.instructions_sent",
							email));
			
			UserHome userDao = new UserHome();

			User user = userDao.findByEmail(email, JPA.em());
			if (user != null) {
				// yep, we have a user with this email that is active - we do
				// not know if the user owning that account has requested this
				// reset, though.
				MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider.getProvider();
				// User exists
				if (user.getEmailValidated()) {
					provider.sendPasswordResetMailing(user, ctx());
					// In case you actually want to let (the unknown person)
					// know whether a user was found/an email was sent, use,
					// change the flash message
				} else {
					// We need to change the message here, otherwise the user
					// does not understand whats going on - we should not verify
					// with the password reset, as a "bad" user could then sign
					// up with a fake email via OAuth and get it verified by an
					// a unsuspecting user that clicks the link.
					flash(Application.FLASH_MESSAGE_KEY,
							Messages.get("playauthenticate.reset_password.message.email_not_verified"));

					// You might want to re-send the verification email here...
					provider.sendVerifyEmailMailingAfterSignup(user, ctx());
				}
			}

			return redirect(routes.Application.index());
		}
	}

	/**
	 * Returns a token object if valid, null if not
	 * 
	 * @param token
	 * @param type
	 * @return
	 */
	private TokenAction tokenIsValid(String token, String type) {
		EntityManager em = JPA.em(JpaConstants.DB);
		
		TokenAction ret = null;
		TokenActionHome tokenDao = new TokenActionHome();
		if (token != null && !token.trim().isEmpty()) {
			TokenAction ta = tokenDao.findByToken(token, type, em);
			if (ta != null && ta.isValid()) {
				ret = ta;
			}
		}

		em.close();
		return ret;
	}

	public Result resetPassword(String token) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		TokenAction ta = tokenIsValid(token, "PASSWORD_RESET");
		if (ta == null) {
			return badRequest(no_token_or_invalid.render());
		}

		return ok(password_reset.render(PASSWORD_RESET_FORM
				.fill(new PasswordReset(token))));
	}

	//@Transactional
	public Result doResetPassword() {
		
		EntityManager em = JPA.em(JpaConstants.DB);
		
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		Form<PasswordReset> filledForm = PASSWORD_RESET_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(password_reset.render(filledForm));
		} else {
			String token = filledForm.get().token;
			String newPassword = filledForm.get().password;

			TokenAction ta = tokenIsValid(token, "PASSWORD_RESET");
			if (ta == null) {
				return badRequest(no_token_or_invalid.render());
			}
			
			TokenActionHome tokenDao = new TokenActionHome();
			
			ta = tokenDao.findById(ta.getId(), em);
			
			String email = ta.getUser().getEmail();
			try {
				// Pass true for the second parameter if you want to
				// automatically create a password and the exception never to
				// happen
				UserHome userDao = new UserHome();
				
				userDao.resetPassword(ta.getUser(), new MyUsernamePasswordAuthUser(newPassword),
						false, em);
			} catch (RuntimeException re) {
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.no_password_account"));
			}
			boolean login = MyUsernamePasswordAuthProvider.getProvider()
					.isLoginAfterPasswordReset();
			
			em.close();
			if (login) {
				// automatically log in
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.success.auto_login"));

				return PlayAuthenticate.loginAndRedirect(ctx(),
						new MyLoginUsernamePasswordAuthUser(email));
			} else {
				// send the user to the login page
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.success.manual_login"));
			}
			return redirect(routes.Application.login());
		}
	}

	public Result oAuthDenied(String getProviderKey) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(oAuthDenied.render(getProviderKey));
	}

	public Result exists() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(exists.render());
	}

	//@Transactional
	public Result verify(String token) {
		
		//TODO work out why @Transactional doesn't like being used more than once
		EntityManager em = JPA.em(JpaConstants.DB);
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		TokenAction ta = tokenIsValid(token, "EMAIL_VERIFICATION");
		if (ta == null) {
			return badRequest(no_token_or_invalid.render());
		}
		TokenActionHome tokenDao = new TokenActionHome();
		
		ta = tokenDao.findById(ta.getId(), em);
		
		String email = ta.getUser().getEmail();
		
		UserHome userDao = new UserHome();
		userDao.verify(ta.getUser(), em);
		flash(Application.FLASH_MESSAGE_KEY,
				Messages.get("playauthenticate.verify_email.success", email));
		
		em.close();
		if (Application.getLocalUser(session()) != null) {
			return redirect(routes.Application.index());
		} else {
			return redirect(routes.Application.login());
		}
	}
}

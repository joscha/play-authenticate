package controllers;

import models.User;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import play.data.Form;
import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthUser;
import views.html.account.*;

import static play.data.Form.form;

public class Account extends Controller {

	public static class Accept {

		@Required
		@NonEmpty
		public Boolean accept;

		public Boolean getAccept() {
			return accept;
		}

		public void setAccept(Boolean accept) {
			this.accept = accept;
		}

	}

	public static class PasswordChange {
		@MinLength(5)
		@Required
		public String password;

		@MinLength(5)
		@Required
		public String repeatPassword;

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getRepeatPassword() {
			return repeatPassword;
		}

		public void setRepeatPassword(String repeatPassword) {
			this.repeatPassword = repeatPassword;
		}

		public String validate() {
			if (password == null || !password.equals(repeatPassword)) {
				return Messages
						.get("playauthenticate.change_password.error.passwords_not_same");
			}
			return null;
		}
	}

	private static final Form<Accept> ACCEPT_FORM = form(Accept.class);
	private static final Form<Account.PasswordChange> PASSWORD_CHANGE_FORM = form(Account.PasswordChange.class);

	@SubjectPresent
	public static Result link() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(link.render());
	}

	@Restrict(@Group(Application.USER_ROLE))
	public static Result verifyEmail() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final User user = Application.getLocalUser(session());
		if (user.emailValidated) {
			// E-Mail has been validated already
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.verify_email.error.already_validated"));
		} else if (user.email != null && !user.email.trim().isEmpty()) {
			flash(Application.FLASH_MESSAGE_KEY, Messages.get(
					"playauthenticate.verify_email.message.instructions_sent",
					user.email));
			MyUsernamePasswordAuthProvider.getProvider()
					.sendVerifyEmailMailingAfterSignup(user, ctx());
		} else {
			flash(Application.FLASH_MESSAGE_KEY, Messages.get(
					"playauthenticate.verify_email.error.set_email_first",
					user.email));
		}
		return redirect(routes.Application.profile());
	}

	@Restrict(@Group(Application.USER_ROLE))
	public static Result changePassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final User u = Application.getLocalUser(session());

		if (!u.emailValidated) {
			return ok(unverified.render());
		} else {
			return ok(password_change.render(PASSWORD_CHANGE_FORM));
		}
	}

	@Restrict(@Group(Application.USER_ROLE))
	public static Result doChangePassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<Account.PasswordChange> filledForm = PASSWORD_CHANGE_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(password_change.render(filledForm));
		} else {
			final User user = Application.getLocalUser(session());
			final String newPassword = filledForm.get().password;
			user.changePassword(new MyUsernamePasswordAuthUser(newPassword),
					true);
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.change_password.success"));
			return redirect(routes.Application.profile());
		}
	}

	@SubjectPresent
	public static Result askLink() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}
		return ok(ask_link.render(ACCEPT_FORM, u));
	}

	@SubjectPresent
	public static Result doLink() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}

		final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(ask_link.render(filledForm, u));
		} else {
			// User made a choice :)
			final boolean link = filledForm.get().accept;
			if (link) {
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.accounts.link.success"));
			}
			return PlayAuthenticate.link(ctx(), link);
		}
	}

	@SubjectPresent
	public static Result askMerge() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}

		// You could also get the local user object here via
		// User.findByAuthUserIdentity(newUser)
		return ok(ask_merge.render(ACCEPT_FORM, aUser, bUser));
	}

	@SubjectPresent
	public static Result doMerge() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}

		final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to merge or not merge
			return badRequest(ask_merge.render(filledForm, aUser, bUser));
		} else {
			// User made a choice :)
			final boolean merge = filledForm.get().accept;
			if (merge) {
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.accounts.merge.success"));
			}
			return PlayAuthenticate.merge(ctx(), merge);
		}
	}

}

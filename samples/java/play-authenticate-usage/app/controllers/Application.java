package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import play.data.Form;
import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.Required;
import play.mvc.*;

import views.html.*;

import be.objectify.deadbolt.actions.Restrict;
import be.objectify.deadbolt.actions.RoleHolderPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

public class Application extends Controller {

	public static final String USER_ROLE = "user";

	public static class Accept {

		@Required
		@NonEmpty
		public Boolean accept;

	}

	public static Form<Accept> ACCEPT_FORM = form(Accept.class);

	public static Result index() {
		return ok(index.render());
	}

	@Restrict("user")
	public static Result restricted() {
		return ok(restricted.render());
	}

	public static Result login() {
		return ok(login.render());
	}

	@Restrict("user")
	public static Result add() {
		return ok(add.render());
	}

	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

	public static Result logout() {
		PlayAuthenticate.logout(session());
		return redirect(routes.Application.index());
	}

	@RoleHolderPresent
	public static Result link() {
		final AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}
		return ok(ask_link.render(ACCEPT_FORM, u));
	}

	@RoleHolderPresent
	public static Result doLink() {
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
			return PlayAuthenticate.link(ctx(), filledForm.get().accept);
		}
	}

	@RoleHolderPresent
	public static Result merge() {
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

	@RoleHolderPresent
	public static Result doMerge() {
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
			return PlayAuthenticate.merge(ctx(), filledForm.get().accept);
		}
	}

}
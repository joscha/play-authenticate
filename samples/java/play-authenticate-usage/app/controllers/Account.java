package controllers;

import be.objectify.deadbolt.actions.Restrict;
import be.objectify.deadbolt.actions.RoleHolderPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import play.data.Form;
import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.account.*;

public class Account extends Controller {

	public static class Accept {

		@Required
		@NonEmpty
		public Boolean accept;

	}
	
	public static Form<Accept> ACCEPT_FORM = form(Accept.class);
	
	@Restrict("user")
	public static Result link() {
		return ok(link.render());
	}

	@RoleHolderPresent
	public static Result askLink() {
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
			final boolean link = filledForm.get().accept;
			if(link) {
				flash("message","Account linked successfully");
			}
			return PlayAuthenticate.link(ctx(), link);
		}
	}

	@RoleHolderPresent
	public static Result askMerge() {
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
			final boolean merge = filledForm.get().accept;
			if(merge) {
				flash("message","Accounts merged successfully");
			}
			return PlayAuthenticate.merge(ctx(), merge);
		}
	}

}

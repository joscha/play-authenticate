package controllers;

import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

public class Secured extends Security.Authenticator {

	@Override
	public String getUsername(final Context ctx) {
		final AuthUser u = PlayAuthenticate.getUser(ctx.session());

		if (u != null) {
			return u.getId();
		} else {
			return null;
		}
	}

	@Override
	public Result onUnauthorized(final Context ctx) {
		ctx.flash().put(Application.FLASH_MESSAGE_KEY, "Nice try, but you need to log in first!");
		return redirect(routes.Application.index());
	}
}
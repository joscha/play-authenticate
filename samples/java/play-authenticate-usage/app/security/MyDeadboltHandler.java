package security;

import models.User;
import play.libs.F;
import play.mvc.Http;
import play.mvc.SimpleResult;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.core.models.Subject;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;

public class MyDeadboltHandler extends AbstractDeadboltHandler {

	@Override
	public F.Promise<SimpleResult> beforeAuthCheck(final Http.Context context) {
		if (PlayAuthenticate.isLoggedIn(context.session())) {
			// user is logged in
			return F.Promise.pure(null); 
		} else {
			// user is not logged in

			// call this if you want to redirect your visitor to the page that
			// was requested before sending him to the login page
			// if you don't call this, the user will get redirected to the page
			// defined by your resolver
			final String originalUrl = PlayAuthenticate
					.storeOriginalUrl(context);

			context.flash().put("error",
					"You need to log in first, to view '" + originalUrl + "'");
			return F.Promise.promise(new F.Function0<SimpleResult>() {
				@Override
				public SimpleResult apply() throws Throwable {
					return redirect(PlayAuthenticate.getResolver().login());
				}
			});
		}
	}

	@Override
	public Subject getSubject(final Http.Context context) {
		final AuthUserIdentity u = PlayAuthenticate.getUser(context);
		// Caching might be a good idea here
		return User.findByAuthUserIdentity(u);
	}

	@Override
	public DynamicResourceHandler getDynamicResourceHandler(
			final Http.Context context) {
		return null;
	}

	@Override
	public F.Promise<SimpleResult> onAuthFailure(final Http.Context context,
			final String content) {
		// if the user has a cookie with a valid user and the local user has
		// been deactivated/deleted in between, it is possible that this gets
		// shown. You might want to consider to sign the user out in this case.
		return F.Promise.promise(new F.Function0<SimpleResult>() {
			@Override
			public SimpleResult apply() throws Throwable {
				return forbidden("Forbidden");
			}
		});
	}
}

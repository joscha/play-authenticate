package security;

import java.util.Optional;

import models.User;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Result;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.core.models.Subject;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;

public class MyDeadboltHandler extends AbstractDeadboltHandler {

	@Override
	public Promise<Optional<Result>> beforeAuthCheck(final Http.Context context) {
		if (PlayAuthenticate.isLoggedIn(context.session())) {
			// user is logged in
			return F.Promise.pure(Optional.empty());
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
            return F.Promise.promise(new F.Function0<Optional<Result>>()
            {
                @Override
                public Optional<Result> apply() throws Throwable
                {
                    return Optional.ofNullable(redirect(PlayAuthenticate.getResolver().login()));
                }
            });
		}
	}

	@Override
	public Promise<Optional<Subject>> getSubject(final Http.Context context) {
		final AuthUserIdentity u = PlayAuthenticate.getUser(context);
		// Caching might be a good idea here
		return F.Promise.pure(Optional.ofNullable((Subject)User.findByAuthUserIdentity(u)));
	}

	@Override
	public Promise<Optional<DynamicResourceHandler>> getDynamicResourceHandler(
			final Http.Context context) {
		return Promise.pure(Optional.empty());
	}

	@Override
	public F.Promise<Result> onAuthFailure(final Http.Context context,
			final String content) {
		// if the user has a cookie with a valid user and the local user has
		// been deactivated/deleted in between, it is possible that this gets
		// shown. You might want to consider to sign the user out in this case.
        return F.Promise.promise(new F.Function0<Result>()
        {
            @Override
            public Result apply() throws Throwable
            {
                return forbidden("Forbidden");
            }
        });
	}
}

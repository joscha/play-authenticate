package security;

import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.models.Subject;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;
import models.User;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class MyDeadboltHandler extends AbstractDeadboltHandler {

	private final PlayAuthenticate auth;

	public MyDeadboltHandler(final PlayAuthenticate auth, final ExecutionContextProvider exContextProvider) {
		super(exContextProvider);
		this.auth = auth;
	}

	@Override
	public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context) {
		if (this.auth.isLoggedIn(context.session())) {
			// user is logged in
			return CompletableFuture.completedFuture(Optional.empty());
		} else {
			// user is not logged in

			// call this if you want to redirect your visitor to the page that
			// was requested before sending him to the login page
			// if you don't call this, the user will get redirected to the page
			// defined by your resolver
			final String originalUrl = this.auth.storeOriginalUrl(context);

			context.flash().put("error",
					"You need to log in first, to view '" + originalUrl + "'");
			return CompletableFuture.completedFuture(Optional.ofNullable(redirect(this.auth.getResolver().login())));
		}
	}

	@Override
	public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context) {
		final AuthUserIdentity u = this.auth.getUser(context);
		// Caching might be a good idea here
		return CompletableFuture.completedFuture(Optional.ofNullable((Subject)User.findByAuthUserIdentity(u)));
	}

	@Override
	public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(
			final Http.Context context) {
		return CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	public CompletionStage<Result> onAuthFailure(final Http.Context context,
												 final Optional<String> content) {
		// if the user has a cookie with a valid user and the local user has
		// been deactivated/deleted in between, it is possible that this gets
		// shown. You might want to consider to sign the user out in this case.
        return CompletableFuture.completedFuture(forbidden("Forbidden"));
	}
}

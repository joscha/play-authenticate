package com.feth.play.module.pa.providers.password;

import org.mindrot.jbcrypt.BCrypt;

import play.Application;
import play.data.Form;
import play.mvc.Call;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.user.AuthUser;

public abstract class UsernamePasswordAuthProvider<UL extends UsernamePasswordAuthUser, US extends UsernamePasswordAuthUser, L extends UsernamePasswordAuthProvider.UsernamePassword, S extends UsernamePasswordAuthProvider.UsernamePassword>
		extends AuthProvider {

	static final String PROVIDER_KEY = "password";

	private static final Object CASE_SIGNUP = new Object();
	private static final Object CASE_LOGIN = new Object();

	protected enum SignupResult {
		USER_EXISTS, USER_UNVERIFIED, USER_CREATED
	}

	protected enum LoginResult {
		USER_UNVERIFIED, USER_LOGGED_IN, NOT_FOUND, WRONG_PASSWORD
	}
	
	public static interface UsernamePassword {

		public String getEmail();

		public String getPassword();
	}

	public UsernamePasswordAuthProvider(final Application app) {
		super(app);
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	public Object authenticate(final Context context, final Object payload)
			throws AuthException {

		if (payload == CASE_SIGNUP) {
			final S signup = getSignup(context.request());
			final US authUser = buildSignupAuthUser(signup);
			final SignupResult r = signupUser(authUser);

			switch (r) {
			case USER_EXISTS:
				// The user exists already
				return userExists(authUser).url();
			case USER_UNVERIFIED:
				// TODO: resend validation email after X minutes
				return userUnverified(authUser).url();
			case USER_CREATED:
				// Send validation email
				// continue to login...
				return authUser;
			default:
				throw new AuthException("Something in signup went wrong");
			}
		} else if (payload == CASE_LOGIN) {
			final L login = getLogin(context.request());
			final UL authUser = buildLoginAuthUser(login);
			final LoginResult r = loginUser(authUser);
			switch (r) {
			case USER_UNVERIFIED:
				// The email of the user is not verified, yet - we won't allow him to log in
				return userUnverified(authUser).url();
			case USER_LOGGED_IN:
				// The user exists and the given password was correct
				return authUser;
			case WRONG_PASSWORD:
				// don't expose this - it might harm users privacy if anyone knows they signed up for our service
			case NOT_FOUND:
				
				context.flash().put("error", "User could not be found or password was wrong.");
				return PlayAuthenticate.getResolver().login().url();
			default:
				throw new AuthException("Something in login went wrong");
			}
		} else {
			return PlayAuthenticate.getResolver().login().url();
		}
	}

	public static Result handleLogin(final Context ctx) {
		return PlayAuthenticate.handleAuthentication(PROVIDER_KEY, ctx,
				CASE_LOGIN);
	}
	
	@Override
	public AuthUser getSessionAuthUser(final String id, final long expires) {
		// TODO implement expiry and use a custom impl
		return new DefaultUsernamePasswordAuthUser(null, id);
	}

	public static Result handleSignup(final Context ctx) {
		return PlayAuthenticate.handleAuthentication(PROVIDER_KEY, ctx,
				CASE_SIGNUP);
	}

	private S getSignup(final Request request) {
		final Form<S> filledForm = getSignupForm().bindFromRequest(request);
		return filledForm.get();
	}

	private L getLogin(final Request request) {
		final Form<L> filledForm = getLoginForm().bindFromRequest(request);
		return filledForm.get();
	}
	
	protected abstract UL buildLoginAuthUser(final L login);

	protected abstract US buildSignupAuthUser(final S signup);
	
	protected abstract LoginResult loginUser(final UL authUser);

	protected abstract SignupResult signupUser(final US user);

	protected abstract Form<S> getSignupForm();

	protected abstract Form<L> getLoginForm();

	protected abstract Call userExists(final UsernamePasswordAuthUser authUser);

	protected abstract Call userUnverified(
			final UsernamePasswordAuthUser authUser);

}

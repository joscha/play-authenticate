package providers;

import org.mindrot.jbcrypt.BCrypt;

import models.LinkedAccount;
import models.User;
import play.Application;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.mvc.Call;
import play.mvc.Controller;
import providers.MyUsernamePasswordAuthProvider.MyLogin.LoginGroup;
import providers.MyUsernamePasswordAuthProvider.MyLogin.SignupGroup;

import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;

import controllers.routes;

public class MyUsernamePasswordAuthProvider
		extends
		UsernamePasswordAuthProvider<DefaultUsernamePasswordAuthUser, MyUsernamePasswordAuthUser, MyUsernamePasswordAuthProvider.MyLogin, MyUsernamePasswordAuthProvider.MySignup> {

	public static class MyLogin
			implements
			com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.UsernamePassword {
		public static interface SignupGroup {
		}

		public static interface LoginGroup {
		}

		@Required(groups = { SignupGroup.class, LoginGroup.class })
		@Email(groups = { SignupGroup.class, LoginGroup.class })
		public String email;

		@Required(groups = { SignupGroup.class, LoginGroup.class })
		@MinLength(value = 5, groups = { SignupGroup.class })
		public String password;

		@Required(groups = { SignupGroup.class })
		@MinLength(value = 5, groups = { SignupGroup.class })
		public String repeatPassword;

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
		@Required(groups = { SignupGroup.class })
		public String name;

		public String validate() {
			if (!password.equals(repeatPassword)) {
				return "Passwords don't match!";
			}
			return null;
		}
	}

	public static Form<MySignup> SIGNUP_FORM = Controller.form(MySignup.class,
			SignupGroup.class);
	public static Form<MyLogin> LOGIN_FORM = Controller.form(MyLogin.class,
			LoginGroup.class);

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
	protected SignupResult signupUser(final MyUsernamePasswordAuthUser user) {
		final User u = User.findByEmail(user.getEmail());
		if (u != null) {
			if (u.emailValidated) {
				// This user exists, has its email validated and is active
				return SignupResult.USER_EXISTS;
			} else {
				// this user exists, is active but has not yet validated its
				// email
				return SignupResult.USER_UNVERIFIED;
			}
		}
		// The user either does not exist or is inactive - create a new one
		final User newUser = User.create(user);
		// Usually the email should be verified before allowing login, however
		// if you return
		// return SignupResult.USER_CREATED;
		// then the user gets logged in directly
		return SignupResult.USER_UNVERIFIED;
	}

	@Override
	protected LoginResult loginUser(final DefaultUsernamePasswordAuthUser authUser) {
		final User u = User.findByUsernamePasswordIdentity(authUser);
		if (u == null) {
			return LoginResult.NOT_FOUND;
		} else {
			if (!u.emailValidated) {
				return LoginResult.USER_UNVERIFIED;
			} else {
				for (final LinkedAccount acc : u.linkedAccounts) {
					if (getKey().equals(acc.providerKey)) {
						if (authUser.checkPassword(acc.providerUserId, authUser.getPassword())) {
							// Password was correct
							return LoginResult.USER_LOGGED_IN;
						} else {
							// if you don't return here,
							// you would allow the user to have
							// multiple passwords defined
							// usually we don't want this
							return LoginResult.WRONG_PASSWORD;
						}
					}
				}
				return LoginResult.WRONG_PASSWORD;
			}
		}
	}

	@Override
	protected Call userExists(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.exists();
	}

	@Override
	protected Call userUnverified(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.unverified();
	}

	@Override
	protected MyUsernamePasswordAuthUser buildSignupAuthUser(final MySignup signup) {
		return new MyUsernamePasswordAuthUser(signup);
	}

	@Override
	protected DefaultUsernamePasswordAuthUser buildLoginAuthUser(MyLogin login) {
		return new DefaultUsernamePasswordAuthUser(login.getPassword(), login.getEmail());
	}
}

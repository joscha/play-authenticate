package com.feth.play.module.pa.providers.password;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import play.Application;
import play.Configuration;
import play.data.Form;
import play.libs.Akka;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;
import akka.actor.Cancellable;
import akka.util.Duration;
import akka.util.FiniteDuration;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.Mailer.Mail;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.Mailer.Mail.Body;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.NameIdentity;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;

public abstract class UsernamePasswordAuthProvider<R, UL extends UsernamePasswordAuthUser, US extends UsernamePasswordAuthUser, L extends UsernamePasswordAuthProvider.UsernamePassword, S extends UsernamePasswordAuthProvider.UsernamePassword>
		extends AuthProvider {
	
	protected static final String PROVIDER_KEY = "password";

	protected static final String SETTING_KEY_MAIL = "mail";

	private static final String SETTING_KEY_MAIL_FROM_EMAIL = "email";

	private static final String SETTING_KEY_MAIL_FROM_NAME = "name";

	private static final String SETTING_KEY_MAIL_DELAY = "delay";

	private static final String SETTING_KEY_MAIL_FROM = "from";
	
	@Override
	protected List<String> neededSettingKeys() {
		return Arrays.asList(SETTING_KEY_MAIL + "." + SETTING_KEY_MAIL_DELAY,
				SETTING_KEY_MAIL + "." + SETTING_KEY_MAIL_FROM + "."
						+ SETTING_KEY_MAIL_FROM_EMAIL);
	}

	public static class Mailer {

		private final MailerPlugin plugin;

		private final FiniteDuration delay;

		private final String sender;

		public Mailer(final MailerPlugin plugin, final Configuration config) {
			this.plugin = plugin;
			// TODO exchange by config.getLong after 2.1 release
			delay = Duration.create(
					Long.parseLong(config.getString(SETTING_KEY_MAIL_DELAY)),
					TimeUnit.SECONDS);

			final Configuration fromConfig = config
					.getConfig(SETTING_KEY_MAIL_FROM);
			sender = getEmailName(
					fromConfig.getString(SETTING_KEY_MAIL_FROM_EMAIL),
					fromConfig.getString(SETTING_KEY_MAIL_FROM_NAME));
		}

		public static class Mail {

			public static class Body {
				private final String html;
				private final String text;
				private final boolean isHtml;
				private final boolean isText;

				public Body(final String text) {
					this(text, null);
				}

				public Body(final String text, final String html) {
					this.isHtml = html != null && !html.trim().isEmpty();
					this.isText = text != null && !text.trim().isEmpty();

					if (!this.isHtml && !this.isText) {
						throw new RuntimeException(
								"Text and HTML cannot both be empty or null");
					}
					this.html = (this.isHtml) ? html : null;
					this.text = (this.isText) ? text : null;
				}

				public boolean isHtml() {
					return isHtml;
				}

				public boolean isText() {
					return isText;
				}

				public boolean isBoth() {
					return isText() && isHtml();
				}

				public String getHtml() {
					return html;
				}

				public String getText() {
					return text;
				}
			}

			private final String subject;
			private final String[] recipients;
			private String from;
			private final Body body;

			public Mail(final String subject, final Body body,
					final String[] recipients) {
				if (subject == null || subject.trim().isEmpty()) {
					throw new RuntimeException(
							"Subject must not be null or empty");
				}
				this.subject = subject;

				if (body == null) {
					throw new RuntimeException("Body must not be null or empty");
				}

				this.body = body;

				if (recipients == null || recipients.length == 0) {
					throw new RuntimeException(
							"There must be at least one recipient");
				}
				this.recipients = recipients;
			}

			public String getSubject() {
				return subject;
			}

			public String[] getRecipients() {
				return recipients;
			}

			public String getFrom() {
				return from;
			}

			private void setFrom(final String from) {
				this.from = from;
			}

			public Body getBody() {
				return body;
			}

		}

		private class MailJob implements Runnable {

			private Mail mail;

			public MailJob(final Mail m) {
				mail = m;
			}

			@Override
			public void run() {
				final MailerAPI api = plugin.email();

				api.setSubject(mail.getSubject());
				api.addRecipient(mail.getRecipients());
				api.addFrom(mail.getFrom());

				if (mail.getBody().isBoth()) {
					// sends both text and html
					api.send(mail.getBody().getText(), mail.getBody().getHtml());
				} else if (mail.getBody().isText()) {
					// sends text/text
					api.send(mail.getBody().getText());
				} else {
					// if(mail.isHtml())
					// sends html
					api.sendHtml(mail.getBody().getHtml());
				}
			}

		}

		public Cancellable sendMail(final Mail email) {
			email.setFrom(sender);
			return Akka.system().scheduler().scheduleOnce(delay, new MailJob(email));
		}

		public Cancellable sendMail(final String subject, final Body body,
				final String recipient) {
			final Mail mail = new Mail(subject, body,
					new String[] { recipient });
			return sendMail(mail);
		}

	}

	protected Mailer mailer;

	private enum Case {
		SIGNUP, LOGIN
	}

	protected enum SignupResult {
		USER_EXISTS, USER_CREATED_UNVERIFIED, USER_CREATED, USER_EXISTS_UNVERIFIED
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
	public void onStart() {
		super.onStart();
		mailer = new Mailer(play.Play.application().plugin(MailerPlugin.class),
				getConfiguration().getConfig(SETTING_KEY_MAIL));
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	public Object authenticate(final Context context, final Object payload)
			throws AuthException {

		if (payload == Case.SIGNUP) {
			final S signup = getSignup(context);
			final US authUser = buildSignupAuthUser(signup);
			final SignupResult r = signupUser(authUser);

			switch (r) {
			case USER_EXISTS:
				// The user exists already
				return userExists(authUser).url();
			case USER_EXISTS_UNVERIFIED:
			case USER_CREATED_UNVERIFIED:
				// User got created as unverified
				// Send validation email
				sendVerifyEmailMailing(context, authUser);
				return userUnverified(authUser).url();
			case USER_CREATED:
				// continue to login...
				return authUser;
			default:
				throw new AuthException("Something in signup went wrong");
			}
		} else if (payload == Case.LOGIN) {
			final L login = getLogin(context);
			final UL authUser = buildLoginAuthUser(login);
			final LoginResult r = loginUser(authUser);
			switch (r) {
			case USER_UNVERIFIED:
				// The email of the user is not verified, yet - we won't allow
				// him to log in
				return userUnverified(authUser).url();
			case USER_LOGGED_IN:
				// The user exists and the given password was correct
				return authUser;
			case WRONG_PASSWORD:
				// don't expose this - it might harm users privacy if anyone
				// knows they signed up for our service
			case NOT_FOUND:
				// forward to login page
				return onLoginUserNotFound(context);
			default:
				throw new AuthException("Something in login went wrong");
			}
		} else {
			return PlayAuthenticate.getResolver().login().url();
		}
	}

	protected String onLoginUserNotFound(Context context) {
		return PlayAuthenticate.getResolver().login().url();
	}

	public static Result handleLogin(final Context ctx) {
		return PlayAuthenticate.handleAuthentication(PROVIDER_KEY, ctx,
				Case.LOGIN);
	}

	@Override
	public AuthUser getSessionAuthUser(final String id, final long expires) {
		return new SessionUsernamePasswordAuthUser(getKey(), id, expires);
	}

	public static Result handleSignup(final Context ctx) {
		return PlayAuthenticate.handleAuthentication(PROVIDER_KEY, ctx,
				Case.SIGNUP);
	}

	private S getSignup(final Context ctx) {
		// TODO change to getSignupForm().bindFromRequest(request) after 2.1
		Http.Context.current.set(ctx);
		final Form<S> filledForm = getSignupForm().bindFromRequest();
		return filledForm.get();
	}

	private L getLogin(final Context ctx) {
		// TODO change to getSignupForm().bindFromRequest(request) after 2.1
		Http.Context.current.set(ctx);
		final Form<L> filledForm = getLoginForm().bindFromRequest();
		return filledForm.get();
	}

	/**
	 * You might overwrite this to provide your own recipient format
	 * implementation,
	 * however the default should be fine for most cases
	 * 
	 * @param user
	 * @return
	 */
	protected String getEmailName(final US user) {
		String name = null;
		if (user instanceof NameIdentity) {
			name = ((NameIdentity) user).getName();
		}

		return getEmailName(user.getEmail(), name);
	}

	protected static String getEmailName(final String email, final String name) {
		if (email == null || email.trim().isEmpty()) {
			throw new RuntimeException("email must not be null");
		}
		final StringBuilder sb = new StringBuilder();
		final boolean hasName = name != null && !name.trim().isEmpty();
		if (hasName) {
			sb.append("\"");
			sb.append(name);
			sb.append("\" <");
		}

		sb.append(email);

		if (hasName) {
			sb.append(">");
		}

		return sb.toString();
	}

	protected abstract R generateVerificationRecord(final US user);

	private void sendVerifyEmailMailing(final Context ctx, final US user) {
		final String subject = getVerifyEmailMailingSubject(user, ctx);
		final R record = generateVerificationRecord(user);
		final Body body = getVerifyEmailMailingBody(record, user, ctx);
		final Mail verifyMail = new Mail(subject, body,
				new String[] { getEmailName(user) });
		mailer.sendMail(verifyMail);
	}

	@Override
	public boolean isExternal() {
		return false;
	}

	protected abstract String getVerifyEmailMailingSubject(final US user,
			final Context ctx);

	protected abstract Body getVerifyEmailMailingBody(
			final R verificationRecord, final US user, final Context ctx);

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

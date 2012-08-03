package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import models.pa_models.TokenAction;
import models.pa_models.TokenAction.Type;
import models.pa_models.User;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import providers.PaLoginUsernamePasswordAuthUser;
import providers.PaUsernamePasswordAuthProvider;
import providers.PaUsernamePasswordAuthProvider.MyIdentity;
import providers.PaUsernamePasswordAuthUser;
import views.html.pa_views.account.signup.*;

public class AuthenticateSignup extends Controller {

    public static class PasswordReset extends AuthenticateAccount.PasswordChange {

        public PasswordReset() {
        }

        public PasswordReset(final String token) {
            this.token = token;
        }

        public String token;
    }

    private static final Form<PasswordReset> PASSWORD_RESET_FORM = form(PasswordReset.class);

    public static Result unverified() {
        return ok(unverified.render());
    }

    private static final Form<MyIdentity> FORGOT_PASSWORD_FORM = form(MyIdentity.class);

    public static Result forgotPassword(final String email) {
        Form<MyIdentity> form = FORGOT_PASSWORD_FORM;
        if (email != null && !email.trim().isEmpty()) {
            form = FORGOT_PASSWORD_FORM.fill(new MyIdentity(email));
        }
        return ok(password_forgot.render(form));
    }

    public static Result doForgotPassword() {
        final Form<MyIdentity> filledForm = FORGOT_PASSWORD_FORM
                .bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not fill in his/her email
            return badRequest(password_forgot.render(filledForm));
        } else {
            // The email address given *BY AN UNKNWON PERSON* to the form - we
            // should find out if we actually have a user with this email
            // address and whether password login is enabled for him/her. Also
            // only send if the email address of the user has been verified.
            final String email = filledForm.get().email;

            // We don't want to expose whether a given email address is signed
            // up, so just say an email has been sent, even though it might not
            // be true - that's protecting our user privacy.
            flash(Authenticate.FLASH_MESSAGE_KEY,
                    Messages.get(
                            "playauthenticate.reset_password.message.instructions_sent",
                            email));

            final User user = User.findByEmail(email);
            if (user != null) {
                // yep, we have a user with this email that is active - we do
                // not know if the user owning that account has requested this
                // reset, though.
                final PaUsernamePasswordAuthProvider provider = PaUsernamePasswordAuthProvider
                        .getProvider();
                // User exists
                if (user.emailValidated) {
                    provider.sendPasswordResetMailing(user, ctx());
                    // In case you actually want to let (the unknown person)
                    // know whether a user was found/an email was sent, use,
                    // change the flash message
                } else {
                    // We need to change the message here, otherwise the user
                    // does not understand whats going on - we should not verify
                    // with the password reset, as a "bad" user could then sign
                    // up with a fake email via OAuth and get it verified by an
                    // a unsuspecting user that clicks the link.
                    flash(Authenticate.FLASH_MESSAGE_KEY,
                            Messages.get("playauthenticate.reset_password.message.email_not_verified"));

                    // You might want to re-send the verification email here...
                    provider.sendVerifyEmailMailingAfterSignup(user, ctx());
                }
            }

            return redirect(routes.Authenticate.index());
        }
    }

    /**
     * Returns a token object if valid, null if not
     *
     * @param token Token
     * @param type type
     * @return mixed
     */
    private static TokenAction tokenIsValid(final String token, final Type type) {
        TokenAction ret = null;
        if (token != null && !token.trim().isEmpty()) {
            final TokenAction ta = TokenAction.findByToken(token, type);
            if (ta != null && ta.isValid()) {
                ret = ta;
            }
        }

        return ret;
    }

    public static Result resetPassword(final String token) {
        final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
        if (ta == null) {
            return badRequest(no_token_or_invalid.render());
        }

        return ok(password_reset.render(PASSWORD_RESET_FORM
                .fill(new PasswordReset(token))));
    }

    public static Result doResetPassword() {
        final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM
                .bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(password_reset.render(filledForm));
        } else {
            final String token = filledForm.get().token;
            final String newPassword = filledForm.get().password;

            final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
            if (ta == null) {
                return badRequest(no_token_or_invalid.render());
            }
            final User u = ta.targetUser;
            try {
                // Pass true for the second parameter if you want to
                // automatically create a password and the exception never to
                // happen
                u.resetPassword(new PaUsernamePasswordAuthUser(newPassword),
                        false);
            } catch (final RuntimeException re) {
                flash(Authenticate.FLASH_MESSAGE_KEY,
                        Messages.get("playauthenticate.reset_password.message.no_password_account"));
            }
            final boolean login = PaUsernamePasswordAuthProvider.getProvider()
                    .isLoginAfterPasswordReset();
            if (login) {
                // automatically log in
                flash(Authenticate.FLASH_MESSAGE_KEY,
                        Messages.get("playauthenticate.reset_password.message.success.auto_login"));

                return PlayAuthenticate.loginAndRedirect(ctx(),
                        new PaLoginUsernamePasswordAuthUser(u.email));
            } else {
                // send the user to the login page
                flash(Authenticate.FLASH_MESSAGE_KEY,
                        Messages.get("playauthenticate.reset_password.message.success.manual_login"));
            }
            return redirect(routes.Authenticate.login());
        }
    }

    public static Result oAuthDenied(final String getProviderKey) {
        return ok(oAuthDenied.render(getProviderKey));
    }

    public static Result exists() {
        return ok(exists.render());
    }

    public static Result verify(final String token) {
        final TokenAction ta = tokenIsValid(token, Type.EMAIL_VERIFICATION);
        if (ta == null) {
            return badRequest(no_token_or_invalid.render());
        }
        final String email = ta.targetUser.email;
        User.verify(ta.targetUser);
        flash(Authenticate.FLASH_MESSAGE_KEY,
                Messages.get("playauthenticate.verify_email.success", email));
        if (Authenticate.getLocalUser(session()) != null) {
            return redirect(routes.Authenticate.index());
        } else {
            return redirect(routes.Authenticate.login());
        }
    }
}

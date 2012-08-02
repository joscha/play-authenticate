package controllers;

import be.objectify.deadbolt.actions.Restrict;
import be.objectify.deadbolt.actions.RoleHolderPresent;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import models.AuthenticateUser;
import models.AuthenticateUser;
import play.data.Form;
import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthUser;
import views.html.authenticate.account.*;

public class AuthenticateAccount extends Controller {

    public static class Accept {

        @Required
        @NonEmpty
        public Boolean accept;

    }

    public static class PasswordChange {
        @MinLength(5)
        @Required
        public String password;

        @MinLength(5)
        @Required
        public String repeatPassword;

        public String validate() {
            if (password == null || !password.equals(repeatPassword)) {
                return Messages.get("playauthenticate.change_password.error.passwords_not_same");
            }
            return null;
        }
    }

    private static final Form<Accept> ACCEPT_FORM = form(Accept.class);
    private static final Form<PasswordChange> PASSWORD_CHANGE_FORM = form(PasswordChange.class);

    @RoleHolderPresent
    public static Result link() {
        return ok(link.render());
    }

    @Restrict(Authenticate.USER_ROLE)
    public static Result verifyEmail() {
        final AuthenticateUser user = Authenticate.getLocalUser(session());
        if (user.emailValidated) {
            // E-Mail has been validated already
            flash(Authenticate.FLASH_MESSAGE_KEY,
                    Messages.get("playauthenticate.verify_email.error.already_validated"));
        } else if (user.email != null && !user.email.trim().isEmpty()) {
            flash(Authenticate.FLASH_MESSAGE_KEY, Messages.get("playauthenticate.verify_email.message.instructions_sent", user.email));
            MyUsernamePasswordAuthProvider.getProvider()
                    .sendVerifyEmailMailingAfterSignup(user, ctx());
        } else {
            flash(Authenticate.FLASH_MESSAGE_KEY, Messages.get(
                    "playauthenticate.verify_email.error.set_email_first",
                    user.email));
        }
        return redirect(routes.Authenticate.profile());
    }

    @Restrict(Authenticate.USER_ROLE)
    public static Result changePassword() {
        final AuthenticateUser u = Authenticate.getLocalUser(session());

        if (!u.emailValidated) {
            return ok(unverified.render());
        } else {
            return ok(password_change.render(PASSWORD_CHANGE_FORM));
        }
    }

    @Restrict(Authenticate.USER_ROLE)
    public static Result doChangePassword() {
        final Form<PasswordChange> filledForm = PASSWORD_CHANGE_FORM
                .bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not select whether to link or not link
            return badRequest(password_change.render(filledForm));
        } else {
            final AuthenticateUser user = Authenticate.getLocalUser(session());
            final String newPassword = filledForm.get().password;
            user.changePassword(new MyUsernamePasswordAuthUser(newPassword),
                    true);
            flash(Authenticate.FLASH_MESSAGE_KEY,
                    Messages.get("playauthenticate.change_password.success"));
            return redirect(routes.Authenticate.profile());
        }
    }

    @RoleHolderPresent
    public static Result askLink() {
        final AuthUser u = PlayAuthenticate.getLinkUser(session());
        if (u == null) {
            // account to link could not be found, silently redirect to login
            return redirect(routes.Authenticate.index());
        }
        return ok(ask_link.render(ACCEPT_FORM, u));
    }

    @RoleHolderPresent
    public static Result doLink() {
        final AuthUser u = PlayAuthenticate.getLinkUser(session());
        if (u == null) {
            // account to link could not be found, silently redirect to login
            return redirect(routes.Authenticate.index());
        }

        final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not select whether to link or not link
            return badRequest(ask_link.render(filledForm, u));
        } else {
            // User made a choice :)
            final boolean link = filledForm.get().accept;
            if (link) {
                flash(Authenticate.FLASH_MESSAGE_KEY,
                        Messages.get("playauthenticate.accounts.link.success"));
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
            return redirect(routes.Authenticate.index());
        }

        // You could also get the local user object here via
        // AuthenticateUser.findByAuthUserIdentity(newUser)
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
            return redirect(routes.Authenticate.index());
        }

        final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not select whether to merge or not merge
            return badRequest(ask_merge.render(filledForm, aUser, bUser));
        } else {
            // User made a choice :)
            final boolean merge = filledForm.get().accept;
            if (merge) {
                flash(Authenticate.FLASH_MESSAGE_KEY,
                        Messages.get("playauthenticate.accounts.merge.success"));
            }
            return PlayAuthenticate.merge(ctx(), merge);
        }
    }

}
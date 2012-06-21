package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import models.User;

import play.data.Form;
import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.Required;
import play.mvc.*;

import views.html.*;

import be.objectify.deadbolt.actions.Restrict;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

public class Application extends Controller {

	public static final String USER_ROLE = "user";

	public static Result index() {
		return ok(index.render());
	}
	
	@Restrict("user")
	public static Result restricted() {
		final User localUser = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
		return ok(restricted.render(localUser));
	}

	public static Result login() {
		return ok(login.render());
	}

	@Restrict("user")
	public static Result add() {
		return ok(add.render());
	}

	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

		}
	}


		if (filledForm.hasErrors()) {
		} else {
		}
	}

	}

	}

}
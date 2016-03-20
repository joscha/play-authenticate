import com.feth.play.module.pa.service.AbstractUserService;
import org.junit.Test;
import play.Application;
import play.Logger;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import providers.TestUsernamePasswordAuthProvider;
import service.TestUserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.*;

public class JavaControllerTest {
	@Test
	public void redirectsWhenNotLoggedIn() {
		Application app = fakeApplication();
		running(app, () -> {
            assertThat(userService(app)).isNotNull();
            Result result = route(controllers.routes.JavaController
					.index());
            assertThat(result.status()).isEqualTo(SEE_OTHER);
        });
	}

	@Test
	public void okWhenLoggedIn() {
		Application app = fakeApplication();
		running(app, () -> {
            assertThat(userService(app)).isNotNull();
            Http.Session session = signupAndLogin(app);

            Result result = route(new RequestBuilder().uri(controllers.routes.JavaController.index().url()).session(session));
            assertThat(result.status()).isEqualTo(OK);
        });
	}

	private Http.Session signupAndLogin(Application app) {
		String email = "user@example.com";
		String password = "PaSSW0rd";
		{
			// Signup with a username/password
			Map<String, String> data = new HashMap<>();
			data.put("email", email);
			data.put("password", password);
			final Call doSignup = controllers.routes.ApplicationController.doSignup();
			Result result = route(new RequestBuilder().method(doSignup.method()).uri(doSignup.url()).bodyForm(data));
			assertThat(result.status()).isEqualTo(SEE_OTHER);
		}
		{
			// Validate the token
			String token = upAuthProvider(app).getVerificationToken(email);
			assertThat(token).isNotNull();
			Logger.info("Verifying token: " + token);
			Result result = route(controllers.routes.ApplicationController
					.verify(token));
			assertThat(result.status()).isEqualTo(SEE_OTHER);
			assertThat(upAuthProvider(app).getVerificationToken(email)).isNull();
			// We should actually be logged in here, but let's ignore that
			// as we want to test login too.
			assertThat(result.redirectLocation()).isEqualTo(Optional.of("/"));
		}
		{
			// Log the user in
			Map<String, String> data = new HashMap<>();
			data.put("email", email);
			data.put("password", password);
			final Call doLogin = controllers.routes.ApplicationController.doLogin();
			Result result = route(new RequestBuilder().method(doLogin.method()).uri(doLogin.url()).bodyForm(data));
			assertThat(result.status()).isEqualTo(SEE_OTHER);
			assertThat(result.redirectLocation()).isEqualTo(Optional.of("/"));
			// Create a Java session from the Scala session
            return result.session();
		}
	}

	private TestUsernamePasswordAuthProvider upAuthProvider(Application app) {
		return app.injector().instanceOf(TestUsernamePasswordAuthProvider.class);
	}

	private AbstractUserService userService(Application app) {
		return app.injector().instanceOf(TestUserService.class);
	}

}

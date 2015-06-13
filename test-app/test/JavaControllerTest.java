import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.route;
import static play.test.Helpers.running;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import play.Logger;
import play.Play;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import providers.TestUsernamePasswordAuthProvider;
import service.TestUserServicePlugin;

import com.feth.play.module.pa.service.UserServicePlugin;

public class JavaControllerTest {
	@Test
	public void redirectsWhenNotLoggedIn() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				assertThat(userServicePlugin()).isNotNull();
				Result result = route(controllers.routes.JavaController
						.index());
				assertThat(result.status()).isEqualTo(SEE_OTHER);
			}
		});
	}

	@Test
	public void okWhenLoggedIn() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				assertThat(userServicePlugin()).isNotNull();
				Http.Session session = signupAndLogin();
				Result result = route(new RequestBuilder().uri(controllers.routes.JavaController.index().url()).session(session));
				assertThat(result.status()).isEqualTo(OK);
			}
		});
	}

	private Http.Session signupAndLogin() {
		String email = "user@example.com";
		String password = "PaSSW0rd";
		{
			// Signup with a username/password
			Map<String, String> data = new HashMap<String, String>();
			data.put("email", email);
			data.put("password", password);
			final Call doSignup = controllers.routes.Application.doSignup();
			Result result = route(new RequestBuilder().method(doSignup.method()).uri(doSignup.url()).bodyForm(data));
			assertThat(result.status()).isEqualTo(SEE_OTHER);
		}
		{
			// Validate the token
			String token = upAuthProvider().getVerificationToken(email);
			assertThat(token).isNotNull();
			Logger.info("Verifying token: " + token);
			Result result = route(controllers.routes.Application
					.verify(token));
			assertThat(result.status()).isEqualTo(SEE_OTHER);
			assertThat(upAuthProvider().getVerificationToken(email)).isNull();
			// We should actually be logged in here, but let's ignore that
			// as we want to test login too.
			assertThat(result.redirectLocation()).isEqualTo("/");
		}
		{
			// Log the user in
			Map<String, String> data = new HashMap<String, String>();
			data.put("email", email);
			data.put("password", password);
			final Call doLogin = controllers.routes.Application.doLogin();
			Result result = route(new RequestBuilder().method(doLogin.method()).uri(doLogin.url()).bodyForm(data));
			assertThat(result.status()).isEqualTo(SEE_OTHER);
			assertThat(result.redirectLocation()).isEqualTo("/");
			// Create a Java session from the Scala session
            return result.session();
		}
	}

	private TestUsernamePasswordAuthProvider upAuthProvider() {
		return Play.application()
				.plugin(TestUsernamePasswordAuthProvider.class);
	}

	private UserServicePlugin userServicePlugin() {
		return Play.application().plugin(TestUserServicePlugin.class);
	}

}

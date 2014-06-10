import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.callAction;
import static play.test.Helpers.session;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.redirectLocation;
import static play.test.Helpers.running;
import static play.test.Helpers.status;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.mail.Session;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.junit.Test;

import akka.util.Timeout;
import org.springframework.scheduling.annotation.AsyncResult;
import play.Logger;
import play.Play;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.test.FakeApplication;
import play.test.FakeRequest;
import play.test.Helpers;
import providers.TestUsernamePasswordAuthProvider;
import scalaz.concurrent.Future;
import service.TestUserServicePlugin;

import com.feth.play.module.pa.service.UserServicePlugin;

public class JavaControllerTest {
	@Test
	public void redirectsWhenNotLoggedIn() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				assertThat(userServicePlugin()).isNotNull();
				Result result = callAction(controllers.routes.ref.JavaController
						.index());
				assertThat(status(result)).isEqualTo(SEE_OTHER);
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
				FakeRequest request = fakeRequest();
				for (Map.Entry<String, String> e : session.entrySet()) {
					request = request.withSession(e.getKey(), e.getValue());
				}
				Result result = callAction(
						controllers.routes.ref.JavaController.index(), request);
				assertThat(status(result)).isEqualTo(OK);
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
			Result result = callAction(
					controllers.routes.ref.Application.doSignup(),
					fakeRequest().withFormUrlEncodedBody(data));
			assertThat(status(result)).isEqualTo(SEE_OTHER);
		}
		{
			// Validate the token
			String token = upAuthProvider().getVerificationToken(email);
			assertThat(token).isNotNull();
			Logger.info("Verifying token: " + token);
			Result result = callAction(controllers.routes.ref.Application
					.verify(token));
			assertThat(status(result)).isEqualTo(SEE_OTHER);
			assertThat(upAuthProvider().getVerificationToken(email)).isNull();
			// We should actually be logged in here, but let's ignore that
			// as we want to test login too.
			assertThat(redirectLocation(result)).isEqualTo("/");
		}
		{
			// Log the user in
			Map<String, String> data = new HashMap<String, String>();
			data.put("email", email);
			data.put("password", password);
			Result result = callAction(
					controllers.routes.ref.Application.doLogin(), fakeRequest()
							.withFormUrlEncodedBody(data));
			assertThat(status(result)).isEqualTo(SEE_OTHER);
			assertThat(redirectLocation(result)).isEqualTo("/");
			// Create a Java session from the Scala session
            return play.test.Helpers.session(result);
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

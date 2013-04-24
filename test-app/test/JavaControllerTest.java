import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.callAction;
import static play.test.Helpers.session;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.running;
import static play.test.Helpers.status;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static scala.collection.JavaConversions.asJavaMap;

import java.util.HashMap;
import java.util.Map;

import javax.mail.Session;

import org.junit.Test;

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
			play.api.mvc.Result actualResult = actualResult(result);
			// We should actually be logged in here, but let's ignore that
			// as we want to test login too.
			assertThat(
					play.api.test.Helpers.redirectLocation(actualResult).get())
					.isEqualTo("/");
		}
		{
			// Log the user in
			Map<String, String> data = new HashMap<String, String>();
			data.put("email", email);
			data.put("password", password);
			Result result = callAction(
					controllers.routes.ref.Application.doLogin(), fakeRequest()
							.withFormUrlEncodedBody(data));
			play.api.mvc.Result actualResult = actualResult(result);
			assertThat(status(result)).isEqualTo(SEE_OTHER);
			assertThat(
					play.api.test.Helpers.redirectLocation(actualResult).get())
					.isEqualTo("/");
			// Create a Java session from the Scala session
			Map<String, String> sessionData =
					asJavaMap(play.api.test.Helpers.session(actualResult)
							.data());
			return new Http.Session(sessionData);
		}
	}

	private play.api.mvc.Result actualResult(Result asyncResult) {
		return (new Promise<play.api.mvc.Result>(
				((play.api.mvc.AsyncResult) asyncResult.getWrappedResult())
						.result())).get();
	}

	private TestUsernamePasswordAuthProvider upAuthProvider() {
		return Play.application()
				.plugin(TestUsernamePasswordAuthProvider.class);
	}

	private UserServicePlugin userServicePlugin() {
		return Play.application().plugin(TestUserServicePlugin.class);
	}

}

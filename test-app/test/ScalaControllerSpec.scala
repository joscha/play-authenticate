package test

import org.specs2.mutable._

import play.api.Logger
import play.api.Play
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Call
import play.api.mvc.Result
import play.api.mvc.Session
import play.api.test._
import play.api.test.Helpers._
import providers.TestUsernamePasswordAuthProvider

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ScalaControllerSpec extends PlaySpecification {

  "ScalaController" should {

    "send 303 for index page without login" in new WithApplication {
      val result = controllers.ScalaController.index()(FakeRequest())
      status(result) must equalTo(SEE_OTHER);
      redirectLocation(result) must beSome like { case Some(s: String) =>
        s must_== controllers.routes.Application.login.url
      }
    }

    "send 200 for index page with login" in new WithApplication {
      val someSession = signupAndLogin()
      val result = controllers.ScalaController.index()(
            FakeRequest().withSession(someSession.get.data.toSeq: _*))
      status(result) must equalTo(OK)
    }

    def signupAndLogin(): Option[Session] = {
      val email = "user@example.com"
      val password = "PaSSW0rd"
      def fakeRequestCall(call: Call): FakeRequest[AnyContentAsEmpty.type] = {
        FakeRequest(call.method, call.url)
      }
      def signup(email: String, password: String) = {
        val someResult = route(fakeRequestCall(
          controllers.routes.Application.doSignup())
          .withFormUrlEncodedBody("email" -> email, "password" -> password))
        someResult foreach { status(_) must_== SEE_OTHER }
        upAuthProvider.getVerificationToken(email)
      }
      def validate(token: String) {
        // Validate the token
        token must not beNull;
        Logger.debug(s"Verifying token: $token")
        val someResult = route(fakeRequestCall(
          controllers.routes.Application.verify(token)))
        someResult foreach { result =>
          status(result) must_== SEE_OTHER
          upAuthProvider.getVerificationToken(email) must beNull
          // We should actually be logged in here, but let's ignore that
          // as we want to test login too.
          redirectLocation(result) must beSome("/")
        }
      }
      def login(username: String, password: String) = {
        // Log the user in
        val someResult = route(fakeRequestCall(
          controllers.routes.Application.doLogin())
          .withFormUrlEncodedBody("email" -> email, "password" -> password))
        someResult map { result =>
          status(result) must_== SEE_OTHER
          redirectLocation(result) must beSome like { case Some(s: String) =>
            s must_== "/"
          }
          session(result)
        }
      }
      val token = signup(email, password)
      validate(token)
      login(email, password)
    }

    def upAuthProvider: TestUsernamePasswordAuthProvider = {
      Play.current.plugin(classOf[TestUsernamePasswordAuthProvider]).get
    }

  }
}

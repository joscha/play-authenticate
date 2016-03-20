package controllers

import javax.inject.Inject

import com.feth.play.module.pa.PlayAuthenticate
import com.feth.play.module.pa.user.AuthUser
import play.api.mvc._
import play.api.mvc.Results.Redirect
import play.api.mvc.Security.Authenticated

import scala.collection.JavaConversions._

class ScalaSecured @Inject() (auth: PlayAuthenticate)  {

  def isAuthenticated(f: => AuthUser => Request[AnyContent] => Result) = {
    Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  private def username(request: RequestHeader) = {
    Option(auth.getUser(javaSession(request)))
  }

  private def onUnauthorized(request: RequestHeader) = {
    Redirect(toScalaCall(auth.getResolver.login))
  }

  private def javaSession(request: RequestHeader): play.mvc.Http.Session = {
    new play.mvc.Http.Session(request.session.data)
  }

  private def toScalaCall(call: play.mvc.Call): Call = {
    Call(call.method, call.url)
  }

}
package controllers

import com.feth.play.module.pa.user.AuthUser
import play.api.mvc.RequestHeader
import play.api.mvc.Results.Redirect
import play.api.mvc.Security.Authenticated
import play.api.mvc.Action
import play.api.mvc.Result
import play.api.mvc.Request
import play.api.mvc.AnyContent
import com.feth.play.module.pa.PlayAuthenticate
import scala.collection.JavaConversions._
import play.api.mvc.Call

object ScalaSecured {

  def isAuthenticated(f: => AuthUser => Request[AnyContent] => Result) = {
    Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  private def username(request: RequestHeader) = {
    Option(PlayAuthenticate.getUser(javaSession(request)))
  }

  private def onUnauthorized(request: RequestHeader) = {
    Redirect(toScalaCall(PlayAuthenticate.getResolver.login))
  }

  private def javaSession(request: RequestHeader): play.mvc.Http.Session = {
    new play.mvc.Http.Session(request.session.data)
  }

  private def toScalaCall(call: play.mvc.Call): Call = {
    Call(call.method, call.url)
  }

}
package security

import be.objectify.deadbolt.scala.{DynamicResourceHandler, DeadboltHandler}
import play.api.mvc.{Request, Result, Results}
import models.User
import be.objectify.deadbolt.core.models.Subject
import com.feth.play.module.pa.PlayAuthenticate
import com.feth.play.module.pa.user.AuthUserIdentity

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
 *
 */
class ScDeadboltHandler(dynamicResourceHandler: Option[DynamicResourceHandler] = None) extends DeadboltHandler {


  override def beforeAuthCheck[A](request: Request[A]) :Option[Future[Result] ]= {

    val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
    if (PlayAuthenticate.isLoggedIn(java_ctx.session)) {
      return None
    }
    else {
      val originalUrl: String = PlayAuthenticate.storeOriginalUrl(java_ctx )
      java_ctx.flash.put("error", "You need to log in first, to view '" + originalUrl + "'")
      val f: Future[Result] = Future {
         Results.Redirect(PlayAuthenticate.getResolver.login.url())
      }
      Some(f)
    }
  }

  override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None


  override def getSubject[A](request: Request[A]): Option[Subject] = {
    // e.g. request.session.get("user")
    val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
    val u: AuthUserIdentity = PlayAuthenticate.getUser(java_ctx)
    Some(  User.findByAuthUserIdentity(u ))
  }

  def onAuthFailure[A](request: Request[A]): Future[Result] = {
    Future{ Results.Forbidden(views.html.restricted())}
  }
}

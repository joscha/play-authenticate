package security

/**
  */
import play.api.mvc._
import scala.concurrent.Future
import play.mvc.Http.Context
import play.core.j.JavaHelpers

object ContextAction extends ActionBuilder[Request] {

  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    Context.current.set(JavaHelpers.createJavaContext(request))
    block(request)
  }
}

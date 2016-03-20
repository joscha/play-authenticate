package controllers

import javax.inject.Inject
import play.api.mvc.Controller

/**
 *
 * The Play Framework doesn't expose something similar to `Enumerator` in Java,
 * or `Ok.feed()` with its `onComplete` and `onError` callback handling.
 *
 * As a result, notifications are implemented in Scala here.
 *
 */
class ScalaController @Inject() (secured: ScalaSecured) extends Controller {

  def index = secured.isAuthenticated { user => implicit request =>
    Ok(s"${user.getProvider()}: ${user.getId()}")
  }

}
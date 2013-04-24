package controllers

import play.api.mvc.Controller
import play.api.mvc.Action

import ScalaSecured.isAuthenticated

/**
 *
 * The Play Framework doesn't expose something similar to `Enumerator` in Java,
 * or `Ok.feed()` with its `onComplete` and `onError` callback handling.
 *
 * As a result, notifications are implemented in Scala here.
 *
 */
object ScalaController extends Controller {

  def index = isAuthenticated { user => implicit request =>
    Ok(s"${user.getProvider()}: ${user.getId()}")
  }

}
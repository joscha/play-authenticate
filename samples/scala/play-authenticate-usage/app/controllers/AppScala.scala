package controllers

import models.User
import play.api.mvc.{Action, Controller}
import security.{ContextAction, ScDeadboltHandler}
import be.objectify.deadbolt.scala.{DeadboltHandler, DeadboltActions}



/**
 */
object AppScala extends Controller with DeadboltActions {

  def index = ContextAction {implicit request =>
    Ok(views.html.index())
  }

  def restrictOne = Restrict(Array(Application.USER_ROLE), new ScDeadboltHandler) {
    ContextAction {implicit request =>
      Ok(views.html.index())
    }
  }
}

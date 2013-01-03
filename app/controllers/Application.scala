package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models.Question
import models.Course
import play.api.libs.openid._
import play.api.libs.concurrent._

object Application extends Controller {
  
  val taskForm = Form(
    "workstationId" -> nonEmptyText
  )

  val loginForm = Form(
    "openid" -> nonEmptyText
  )
  
  val course = new Course("LOG1000")
  
  def index = Action {
    Redirect(routes.Application.questions)
  }
  
  def questions = Action { implicit request =>
    Ok(views.html.index(course, taskForm, session.get("connected")))
  }
  
  def newQuestion = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(course, errors, session.get("connected"))),
      workstationId => {
	course.addQuestion(workstationId)
	Redirect(routes.Application.questions)
      }
    )
  }
  
  def deleteQuestion(id: String) = Action {
    course.removeQuestion(id)
    Redirect(routes.Application.questions)
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm, flash.get("errorMessage").getOrElse("")))
  }

  def loginPost = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      error => {
        Logger.info("bad request " + error.toString)
        BadRequest(error.toString)
      },
      {
        case (openid) => AsyncResult(OpenID.redirectURL(openid, routes.Application.openIDCallback.absoluteURL(),
                                      Seq("email" -> "http://schema.openid.net/contact/email"/*,
                                        "firstname" -> "http://axschema.org/namePerson/first"*/))
            .extend( _.value match {
                case Redeemed(url) => Redirect(url)
                case Thrown(t) => Redirect(routes.Application.login).flashing("errorMessage" -> (t.toString + "fail 1"))
            }))
      }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.Application.questions).withNewSession
  }

  def openIDCallback = Action { implicit request =>
    AsyncResult(
      OpenID.verifiedId.extend( _.value match {
        case Redeemed(info) => Redirect(routes.Application.questions).withSession(
          "connected" -> info.attributes("email")
        )
        case Thrown(t) => {
          // Here you should look at the error, and give feedback to the user
          Redirect(routes.Application.login).flashing("errorMessage" -> (t.toString + "fail 2"))
        }
      })
    )
  }
}
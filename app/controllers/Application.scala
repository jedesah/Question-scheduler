package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models.Question
import models.Course

object Application extends Controller {
  
  val taskForm = Form(
    "workstationId" -> nonEmptyText
  )
  
  val course = new Course("LOG1000")
  
  def index = Action {
    Redirect(routes.Application.questions)
  }
  
  def questions = Action {
    Ok(views.html.index(course, taskForm))
  }
  
  def newQuestion = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(course, errors)),
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
}
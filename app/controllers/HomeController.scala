package controllers

import javax.inject._

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.DataModel._
import play.api.i18n.I18nSupport

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with I18nSupport{

  val contactForm: Form[contactUs] = Form(
    mapping(
      "name" -> text,
      "message" -> optional(text)
    )(contactUs.apply)(contactUs.unapply)
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def profile() = Action { implicit request =>
    Logger.error("Open My Profile!")
    Ok(views.html.profile(contactForm))
  }

  def contactResult(name: String, message: Option[String]) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.result(name, message))
  }

  def contactPost = Action(parse.form(contactForm)) { implicit request =>
    val data = request.body
    Logger.info(data.toString)
    println(data)
    val newUser = contactUs(data.name, data.message)
    //Redirect(routes.HomeController.contactResult(newUser.name, newUser.message))
    Ok(views.html.result(data.name, data.message))
    //Logger.info("AAA")
  }

}

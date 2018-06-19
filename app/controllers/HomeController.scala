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

  val ContactForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> nonEmptyText,
      "phone" -> optional(text),
      "message" -> optional(text)
    )(contactUs.apply)(contactUs.unapply)
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def about() = Action { implicit request =>
    Ok(views.html.about())
  }

  def contactPage() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.contact(ContactForm))
  }

  def contactResult() = Action { implicit request =>
    ContactForm.bindFromRequest.fold(
      formError => {
        Logger.error(s"Form Leave Period error ${formError.toString}")
        BadRequest(views.html.contact(formError))
      },
      formData => {
        Ok(views.html.result(formData.name, formData.email, formData.phone, formData.message))
      }
    )

  }

}

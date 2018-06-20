package controllers

import java.time.ZonedDateTime

import database.dbrepos.{ContactDataRepo, ContactServiceRepo}
import database.dbtable.{ContactData}
import javax.inject._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.DataModel._
import play.api.i18n.I18nSupport
import secure.Authenticated

import scala.concurrent.{ExecutionContext}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               authen: Authenticated,
                               contactServiceRepo: ContactServiceRepo,
                               contactDataRepo: ContactDataRepo
                              )(implicit ec: ExecutionContext)  extends AbstractController(cc) with I18nSupport{

  val ContactForm = Form(
    mapping(
      "service" -> number,
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

  def contactPage() = Action.async { implicit request: Request[AnyContent] =>
    contactServiceRepo.getAll.map{ case (serviceList) =>
      Ok(views.html.contact(ContactForm, serviceList))
    }
  }

  def contactResult() = Action.async { implicit request: Request[AnyContent] =>
    ContactForm.bindFromRequest.fold(
      formError => {
        Logger.error(s"Form Leave Period error ${formError.toString}")
        contactServiceRepo.getAll.map{ case (serviceList) =>
          BadRequest(views.html.contact(formError, serviceList))
        }
      },
      formData => {
        val newObj = ContactData(0, formData.service, formData.name, formData.email, formData.phone, formData.message, ZonedDateTime.now())
        contactDataRepo.insert(newObj).map { id =>
          Ok(views.html.result(newObj, None))
        }.recover { case ex =>
          Ok(views.html.result(newObj, Some(ex.getMessage)))
        }
      }
    )
  }

}

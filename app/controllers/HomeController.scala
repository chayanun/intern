package controllers

import java.time.ZonedDateTime

import database.dbrepos.{ContactDataRepo, ContactServiceRepo}
import database.dbtable.ContactData
import javax.inject._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.DataModel._
import play.api.i18n.I18nSupport
import secure.Authenticated
import utils.MailServer

import scala.concurrent.ExecutionContext


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               authen: Authenticated,
                               contactServiceRepo: ContactServiceRepo,
                               contactDataRepo: ContactDataRepo,
                               mailServer: MailServer
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

  def contactPage(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    contactServiceRepo.getAll.map{ serviceList =>
      Ok(views.html.contact(ContactForm, serviceList))
    }
  }

  def contactResult(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    ContactForm.bindFromRequest.fold(
      formError => {
        Logger.error(s"Form Leave Period error ${formError.toString}")
        contactServiceRepo.getAll.map{ serviceList =>
          BadRequest(views.html.contact(formError, serviceList))
        }
      },
      formData => {
        val newObj = ContactData(0, formData.service, formData.name, formData.email, formData.phone, formData.message, ZonedDateTime.now())
        contactDataRepo.insert(newObj).map { _ =>
          val mailMessage = s"<h2>Hello ${formData.name},</h2>" +
            "<p>Thanks for being awesome!</p>" +
            "<p>We have received your message and would like to thank you for writing to us. If your inquiry is urgent, please use the telephone number listed below to talk to one of our staff members. Otherwise, we will reply by email as soon as possible.</p>" +
            "<p>Talk to you soon,</p>" +
            "<p>[Tradition]</p>" +
            "<hr>" +
            s"<p><b>Name:</b><br>${formData.name}</p>" +
            s"<p><b>Email:</b><br>${formData.email}</p>" +
            s"<p><b>Phone:</b><br>${formData.phone.getOrElse("-")}</p>" +
            s"<p><b>Message:</b><br>${formData.message.getOrElse("-")}</p>"

          mailServer.send(Seq(s"${formData.name} <${formData.email}>"), "Thanks for being awesome!", mailMessage)

          Ok(views.html.result(newObj, None))
        }.recover { case ex =>
          Ok(views.html.result(newObj, Some(ex.getMessage)))
        }
      }
    )
  }

}

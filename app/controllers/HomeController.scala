package controllers

import java.time.ZonedDateTime

import database.dbrepos.{ContactDataRepo, ContactServiceRepo}
import database.dbtable.{ContactData, ContactService}
import javax.inject._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.DataModel._
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
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

  def contactList() = Action.async { implicit request =>
    contactDataRepo.getAllWithService.map{ case data =>
      Ok(views.html.contactList(data))
    }
  }

  def delete(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    try {
      request.body match {
        case (data) =>
          val id = (data \ "id").as[Int]
          contactDataRepo.delete(id).map {
            case Success(_) => Ok(Json.toJson("success"))
            case Failure(e) => Ok(Json.toJson(e.getMessage))
          }
      }
    }catch {
      case e: Exception =>
        Logger.info(s"${e.getMessage}")
        Future(Ok(Json.toJson(e.getMessage)))
    }
  }

  def serviceList() = Action.async { implicit request =>
    contactServiceRepo.getAll.map{ case data =>
      Ok(views.html.service(data))
    }
  }

  def updateService(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    try {
      request.body match {
        case (data) =>
          val id = (data \ "id").as[Int]
          val name = (data \ "name").as[String]
          val updateItem = ContactService(id, name)
          contactServiceRepo.update(updateItem).map {
            case Success(_) => Ok(Json.toJson("success"))
            case Failure(e) => Ok(Json.toJson(e.getMessage))
          }
      }
    }catch {
      case e: Exception =>
        Logger.info(s"${e.getMessage}")
        Future(Ok(Json.toJson(e.getMessage)))
    }
  }

  def deleteService(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    try {
      request.body match {
        case (data) =>
          val id = (data \ "id").as[Int]
          contactServiceRepo.delete(id).map {
            case Success(_) => Ok(Json.toJson("success"))
            case Failure(e) => Ok(Json.toJson(e.getMessage))
          }
      }
    }catch {
      case e: Exception =>
        Logger.info(s"${e.getMessage}")
        Future(Ok(Json.toJson(e.getMessage)))
    }
  }
}

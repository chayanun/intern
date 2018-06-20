package controllers

import com.typesafe.config.ConfigFactory
import database.dbrepos.{ContactDataRepo, ContactServiceRepo, UserLoginRepo}
import database.dbtable.{ContactService, UserLogin}
import javax.inject.Inject
import play.api.Logger
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import secure._
import utils.MessageError

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AdminController @Inject()(cc: ControllerComponents,
                                authen: Authenticated,
                                userLoginRepo: UserLoginRepo,
                                contactServiceRepo: ContactServiceRepo,
                                contactDataRepo: ContactDataRepo
                               )(implicit ec: ExecutionContext)  extends AbstractController(cc) with I18nSupport{

  val frmLogin: Form[(String, String, Boolean)] = Form(
    tuple(
      "username" -> text,
      "password" -> text,
      "rememberMe" -> boolean)
  )

  def login() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login())
  }

  def logout: Action[AnyContent] = authen.sync(NormalUserRole.role, AdministratorRole.role) { implicit request =>
    authen.processSignout
  }
  def gotoLogin(msg: String): Result = {
    Redirect(routes.AdminController.login()).flashing(MessageError(s"$msg").flash)
  }

  def authenticate: Action[AnyContent] = Action.async { implicit request =>

    frmLogin.bindFromRequest.fold(
      _ => Future(gotoLogin("The user name or password is incorrect")),
      loginInfo => {
        val (username, password, _) = loginInfo

        def normalLogin(u: Option[UserLogin]): Future[Result] = {
          u match {
            case None => Future(gotoLogin(s"The username $username not found"))
            case Some(us) =>
              Logger.info("Login with normal")
              authen.otcCreateUserSession(UserName(username))
          }
        }

        for {
          aut <- authen.processOtcAuthenticate(UserName(username), password)
          u <- if (!aut) {
            Future(None)
          } else {
            userLoginRepo.getUserByName(username)
          }
          r <- u match {
            case None =>
              Logger.info(s"The username $username not found")
              Future(gotoLogin(s"The username $username not found"))
            case Some(user) =>
              normalLogin(u)
          }
        } yield r
      })
  }

  def dashboard() = authen.async(List(AdministratorRole.role)) { implicit request: Request[AnyContent] =>
    Future(Ok(views.html.dashboard()))
  }

  def serviceList() = authen.async(List(AdministratorRole.role)) { implicit request =>
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

  def contactList() = authen.async(List(AdministratorRole.role)) { implicit request =>
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
}

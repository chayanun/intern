package secure

import java.time.{ZoneOffset, ZonedDateTime}

import javax.inject.Inject
import controllers.routes
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Failure, Success}
import org.json4s.native.Serialization._
import utils.{Json4sHelper, MessageError}

import scala.util.Try

case class UserName(value: String) extends AnyVal // with MappedTo[String]

case class UserSession(userId: Int, username: UserName, sessionId: String, var lastAction: ZonedDateTime, roles: Set[Int])

case class CookieInfo(userId: Int, userName: UserName, token: String, userAgent: String)

case class AuthenticatedRequest[A](user: UserSession, request: Request[A]) extends WrappedRequest[A](request)

object Authenticated {
  val UsernameKey = "username"
}

class Authenticated @Inject()(val parser: BodyParsers.Default, val scm: SecureManager)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[AuthenticatedRequest, AnyContent] {

  import Authenticated._

  implicit val formats = Json4sHelper.formats

  private val NO_PERMISSION_MSG: String = "User : %s doesn't have permission on %s"
  private val REFERRER_HEADER: String = "referer"

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    request.session.get(UsernameKey) match {
      case None => cookieAuthenticate(request)
      case Some(session) =>
        Try(read[UserSession](session)) match {
          case util.Failure(exception) =>
            Logger.warn(exception.getMessage, exception)
            Logger.warn("InvokeBlock-1")
            Future(onUnauthenticated(request))
          case util.Success(userSession) =>
            if (checkTimeout(userSession)) {
              userSession.lastAction = ZonedDateTime.now(ZoneOffset.UTC)
              block(AuthenticatedRequest(userSession, request))
            } else {
              Logger.warn("InvokeBlock-2")
              Future(onUnauthenticated(request))
            }
        }
    }
  }

  def sync(requireRole: Int*)(block: (AuthenticatedRequest[AnyContent]) => Result): Action[AnyContent] = {
    //apply(block)
    apply {
      (request: AuthenticatedRequest[AnyContent]) =>
        val hasPermission =  requireRole.exists{request.user.roles.contains}
        if (hasPermission) {
          block(request)
        } else {
          val errorMessage = NO_PERMISSION_MSG.format(request.user.username, request.path, requireRole)
          Logger.error(errorMessage)
          Results.Redirect(routes.AdminController.dashboard()).flashing(Flash(Map("error" -> errorMessage)))
        }
    }
  }

  def async(requireRole: List[Int])(block: (AuthenticatedRequest[AnyContent]) => Future[Result]): Action[AnyContent] = {
    async {
      request =>
        val hasPermission =  requireRole.exists{request.user.roles.contains}
        if (hasPermission) {
          block(request)
        } else {
          val errorMessage = NO_PERMISSION_MSG.format(request.user.username, request.path, requireRole)
          Logger.error(errorMessage)
          Future(Results.Redirect(routes.AdminController.dashboard()).flashing(Flash(Map("error" -> errorMessage))))
        }
    }
  }

  def asyncWithFile[A](requireRole: List[Int], bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result]): Action[A] = {
    async(bodyParser) {
      request =>
        val hasPermission =  requireRole.exists{request.user.roles.contains}
        if (hasPermission) {
          block(request)
        } else {
          val errorMessage = NO_PERMISSION_MSG.format(request.user.username, request.path, requireRole)
          Logger.error(errorMessage)
          Future(Results.Redirect(routes.AdminController.dashboard()).flashing(Flash(Map("error" -> errorMessage))))
        }
    }
  }

  private def check(username: UserName, password: String): Future[Boolean] = {
    scm.checkPassword(username.value, password)
  }

  def processAuthenticate(username: UserName, password: String, rememberme: Boolean)(implicit request: RequestHeader): Future[Result] = {

    check(username, password).flatMap {
      isValid =>
        if (isValid) {
          scm.createUserSession(username).map {
            case Success(us) => Logger.info(s"create: $us")

              //              scm.addUser(us)
              if (rememberme) {
                Logger.info("remember me: true")
                val cookie = scm.generateAuthenticationCookie(us, request)
                Results.Redirect(routes.AdminController.dashboard()).withSession(Authenticated.UsernameKey -> write(us)).withCookies(cookie)
              } else {
                Logger.info("remember me: false")
                Results.Redirect(routes.AdminController.dashboard()).withSession(Authenticated.UsernameKey -> write(us)).discardingCookies(DiscardingCookie(scm.AUTH_COOKIE))
              }

            case Failure(e) =>
              val error = e.list.toList.mkString("\n")

              Logger.error(error)
              Results.Redirect(routes.AdminController.login()).flashing("message" -> error)
          }
        } else {
          Future(Results.Redirect(routes.AdminController.login()).flashing(MessageError("The username or password is incorrect.").flash))
        }
    }
  }

  def processOtcAuthenticate(username: UserName, password: String)(implicit request: RequestHeader) = {
    check(username, password).flatMap { isValid =>
      Future(isValid)
    }
  }

  def otcCreateUserSession(username: UserName)(implicit request: RequestHeader) = {
    scm.createUserSession(username).map {
      case Success(us) => Logger.info(s"create: $us")

        Logger.info("remember me: false")
        Results.Redirect(routes.AdminController.dashboard()).withSession(Authenticated.UsernameKey -> write(us)).discardingCookies(DiscardingCookie(scm.AUTH_COOKIE))

      case Failure(e) =>
        val error = e.list.toList.mkString("\n")

        Logger.error(error)
        Results.Redirect(routes.AdminController.login()).flashing("message" -> error)
    }
  }

  def processSignout(implicit request: RequestHeader): Result = {
    //    scm.removeUser(Authenticated.UsernameKey)
    //    scm.removeAuthenticationCookie(request)

    Results.Redirect(routes.AdminController.login()).withNewSession.discardingCookies(DiscardingCookie(scm.AUTH_COOKIE))
  }

  /**
    * Check for authenticated cookie, in case user did "Remember Me"
    */
  private def cookieAuthenticate(request: RequestHeader): Future[Result] = {
    scm.checkAuthenticationCookie(request).map {
      (cookieInfo: CookieInfo) =>

        scm.createUserSession(cookieInfo.userName).map {
          case Success(us) =>

            // set new cookie
            val cookie = scm.generateAuthenticationCookie(us, request)

            // login with cookie success
            Results.Redirect(routes.AdminController.dashboard())
              .withSession(UsernameKey -> write(us))
              .withCookies(cookie)

          case Failure(e) =>
            val error = e.list.toList.mkString("\n")
            Logger.warn("Cookie-1")
            onUnauthenticated(request).flashing("message" -> error)

        }
    }.getOrElse{
      Logger.warn("Cookie-2")
      Future(onUnauthenticated(request))} /* cannot login with auth cookie */
  }

  //  private def onUnauthorized(request: AuthenticatedRequest[AnyContent], requireRole: UserRole): Result = {
  //    val errorMessage = NO_PERMISSION_MSG.format(request.user.username, request.path, requireRole.toString)
  //    Logger.error(errorMessage)
  //    Results.Redirect(request.headers(REFERRER_HEADER)).flashing(Flash(Map("error" -> errorMessage)))
  //  }

  private def onUnauthorized(request: AuthenticatedRequest[AnyContent], requireRole: String): Result = {
    val errorMessage = NO_PERMISSION_MSG.format(request.user.username, request.path, requireRole)
    Logger.error(errorMessage)
    Results.Redirect(request.headers(REFERRER_HEADER)).flashing(Flash(Map("error" -> errorMessage)))
  }

  private def onUnauthenticated(request: RequestHeader): Result = {
    Logger.warn("Unauthenticate request")
    Results.Redirect(routes.AdminController.login())
  }

  private def hasPermission(user: UserSession, requireRole: UserRole): Boolean = {
    user.roles.equals(requireRole)
  }

  private def checkTimeout(userSession: UserSession): Boolean =
    userSession.lastAction.plusMinutes(60).isAfter(ZonedDateTime.now(ZoneOffset.UTC))


}

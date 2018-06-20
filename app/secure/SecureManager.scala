package secure

import javax.inject.{Inject, Singleton}
import database.dbrepos.UserLoginRepo
import play.api.mvc.{Cookie, InjectedController, RequestHeader}
import scalaz.ValidationNel

import scala.concurrent.{ExecutionContext, Future}
import org.json4s.native.Serialization._
import utils.{Helpers, Json4sHelper}

import scala.util.Try

@Singleton
class SecureManager @Inject()(userRepo: UserLoginRepo)(implicit executionContext: ExecutionContext) extends InjectedController {

  val AUTH_COOKIE = "tk"
  val COOKIE_MAX_AGE = 259200 // maxAge = 3 days (259200 sec)
  implicit val formats = Json4sHelper.formats
  def checkAuthenticationCookie(request: RequestHeader): Option[CookieInfo] = {
    // get auth cookie
    val ck = request.cookies.get(AUTH_COOKIE)

    ck.flatMap { c =>
      Try(read[CookieInfo](Helpers.decodeBase64(c.value))).toOption
    }
  }

  def createUserSession(username: UserName): Future[ValidationNel[String, UserSession]] = {
    userRepo.createUserSession(username)
  }

  def checkPassword(username: String, password: String): Future[Boolean] = {
    userRepo.checkPassword(username, password)
  }

  def generateAuthenticationCookie(userSession: UserSession, request: RequestHeader): Cookie = {
    // random token
    val token = PasswordHash.createHash(java.util.UUID.randomUUID.toString)

    // create authenticate cookie
    val userAgent = request.headers.get("user-Agent").getOrElse("")
    val authCookie = PasswordHash.createHash(token + userAgent)
    val cookieInfo = CookieInfo(userSession.userId, userSession.username, token, userAgent)
    val jsonCookie = write(cookieInfo)

    // create http cookie
    Cookie(AUTH_COOKIE, Helpers.encodeBase64(jsonCookie), maxAge = Some(259200))
  }

}


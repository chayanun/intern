package database.dbrepos

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID

import database.dbaccess._
import database.dbtable._
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import secure.{PasswordHash, UserName, UserSession}
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import scalaz._
import Scalaz._


class UserLoginRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                              userLoginAccessor: UserLoginAccessor)
                             (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def getUserByName(username: String): Future[Option[UserLogin]] = {
    db.run(UserLoginTable.filter(_.userName === username).result.headOption)
  }

  def checkPassword(username: String, password: String): Future[Boolean] = {

    val queryDBIO: DBIO[Option[UserLogin]] = userLoginAccessor.getUserByName(username)

    db.run(queryDBIO.transactionally).map {
      case Some(u) => PasswordHash.validatePassword(password, u.password)
      case None => false
    }

  }

  def createUserSession(username: UserName): Future[ValidationNel[String, UserSession]] = {
    val result = userLoginAccessor.getUserByName(username.value)

    db.run(result.transactionally).map { case (user) =>
      user match {
        case None => "User doesn't have Administrator permission".failureNel
        case Some(u) =>
          // extract user's roles
          val roles = extractPermission(u.userRole)
          //WebConstant.SYSTEM_TOTAL_PAGE = setting.totalPerPage
          UserSession(u.userId, username, UUID.randomUUID.toString, ZonedDateTime.now(ZoneOffset.UTC), roles).successNel
      }

    }
  }

  def extractPermission(userRole: Int): Set[Int] = {

    val bs = userRole.toBinaryString.toList.reverse
    Set(bs.zipWithIndex.flatMap { x =>
      val (n, p) = x
      if (n == '1') Some(1 << p) else None
    }: _*)
  }
}

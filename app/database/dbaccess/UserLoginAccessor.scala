package database.dbaccess

import database.dbtable._
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class UserLoginAccessor@Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  def checkUserSession(userSession: Int): DBIO[UserLogin] = {
    UserLoginTable.filter(_.userId === userSession).map(u => u).result.head
  }
  def getUserByName(username: String): DBIO[Option[UserLogin]] = {
    UserLoginTable.filter(_.userName === username).result.headOption
  }
}


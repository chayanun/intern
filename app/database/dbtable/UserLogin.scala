package database.dbtable

import database.driver.PgProfile.api._
import slick.lifted.ProvenShape

case class UserLogin(userId: Int, userName: String, password: String, userRole: Int)
class UserLoginTable(tag: Tag) extends Table[UserLogin](tag, "user_login")  {
  def userId: Rep[Int] = column[Int]("user_id", O.PrimaryKey, O.AutoInc)
  def userName: Rep[String] = column[String]("user_name")
  def password: Rep[String] = column[String]("password")
  def userRole: Rep[Int] = column[Int]("user_role")

  def * : ProvenShape[UserLogin] = (userId, userName, password, userRole) <> (UserLogin.tupled, UserLogin.unapply)
}

object UserLoginTable extends TableQuery(new UserLoginTable(_))

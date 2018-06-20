package database.dbaccess

import javax.inject.Inject
import database.dbtable._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class ContactServiceAccessor@Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                     (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  def getAll: DBIO[List[ContactService]] = {
    ContactServiceTable.sortBy(_.serviceName).result.map(_.toList)
  }

  def update(obj: ContactService): DBIO[Int] = {
    ContactServiceTable.filter(_.serviceId === obj.serviceId).update(obj)
  }

  def delete(id: Int): DBIO[Int] = {
    ContactServiceTable.filter(_.serviceId === id).delete
  }
}


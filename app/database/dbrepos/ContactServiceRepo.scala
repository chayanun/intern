package database.dbrepos

import javax.inject.Inject
import database.dbaccess._
import database.dbtable._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ContactServiceRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                                   contactServiceAccessor: ContactServiceAccessor)
                                  (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def getAll: Future[List[ContactService]] ={
    db.run(contactServiceAccessor.getAll).map(r => r)
  }

  def update(obj: ContactService): Future[Try[Int]] = {
    db.run(contactServiceAccessor.update(obj).asTry)
  }

  def delete(id: Int): Future[Try[Int]] = {
    db.run(contactServiceAccessor.delete(id).asTry)
  }
}

package database.dbrepos

import database.dbaccess._
import database.dbtable._
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ContactDataRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                                contactDataAccessor: ContactDataAccessor)
                               (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  def getAll: Future[List[ContactData]] ={
    db.run(contactDataAccessor.getAll).map(r => r)
  }

  def insert(contactObj: ContactData): Future[Int] ={
    db.run(contactDataAccessor.insert(contactObj))
  }

  def getById(id: Int): Future[Option[(ContactData, ContactService)]] = {
    db.run(contactDataAccessor.getById(id))
  }
}

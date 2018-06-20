package database.dbaccess

import database.dbtable._
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class ContactDataAccessor@Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                  (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  def getAll: DBIO[List[ContactData]] = {
    ContactDataTable.result.map(_.toList)
  }

  def getAllWithService: DBIO[List[(ContactData, ContactService)]]={
    val query = for{
      (d,s) <- ContactDataTable.join(ContactServiceTable).on(_.serviceId === _.serviceId)
    } yield (d, s)

    query.result.map(_.toList)
  }

  def getById(id: Int): DBIO[Option[(ContactData, ContactService)]] = {
    ContactDataTable.filter(_.contactId === id).join(ContactServiceTable).on(_.serviceId === _.serviceId).result.headOption
  }

  def insert(contactObj: ContactData): DBIO[Int] = {
    ContactDataTable returning ContactDataTable.map(_.contactId) += contactObj
  }

  def delete(id: Int): DBIO[Int] = {
    ContactDataTable.filter(_.contactId === id).delete
  }
}


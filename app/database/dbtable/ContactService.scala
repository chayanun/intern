package database.dbtable

import database.driver.PgProfile.api._
import slick.lifted.ProvenShape

case class ContactService(serviceId: Int, serviceName: String)
class ContactServiceTable(tag: Tag) extends Table[ContactService](tag, "contact_service")  {
  def serviceId: Rep[Int] = column[Int]("service_id", O.PrimaryKey, O.AutoInc)
  def serviceName: Rep[String] = column[String]("service_name")

  def * : ProvenShape[ContactService] = (serviceId, serviceName) <> (ContactService.tupled, ContactService.unapply)
}

object ContactServiceTable extends TableQuery(new ContactServiceTable(_))

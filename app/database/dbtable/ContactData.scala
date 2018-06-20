package database.dbtable

import java.time.ZonedDateTime

import database.driver.PgProfile.api._
import slick.lifted.ProvenShape

case class ContactData(contactId: Int, serviceId: Int, contactName: String, contactEmail: String,
                       contactPhone: Option[String], contactMessage: Option[String], createdDate: ZonedDateTime)
class ContactDataTable(tag: Tag) extends Table[ContactData](tag, "contact_data")  {
  def contactId: Rep[Int] = column[Int]("contact_id", O.PrimaryKey, O.AutoInc)
  def serviceId: Rep[Int] = column[Int]("contact_service_id")
  def contactName: Rep[String] = column[String]("contact_name")
  def contactEmail: Rep[String] = column[String]("contact_email")
  def contactPhone: Rep[Option[String]] = column[Option[String]]("contact_phone")
  def contactMessage: Rep[Option[String]] = column[Option[String]]("contact_message")
  def createdDate: Rep[ZonedDateTime] = column[ZonedDateTime]("created_date")


  def * : ProvenShape[ContactData] = (contactId, serviceId, contactName, contactEmail, contactPhone, contactMessage, createdDate) <> (ContactData.tupled, ContactData.unapply)
}

object ContactDataTable extends TableQuery(new ContactDataTable(_))

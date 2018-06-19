package models

object DataModel {
  case class contactUs(name: String, email: String, phone: Option[String],message: Option[String])
}

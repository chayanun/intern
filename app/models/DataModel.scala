package models

object DataModel {
  case class contactUs(service: Int, name: String, email: String, phone: Option[String],message: Option[String])
}

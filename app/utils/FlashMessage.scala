package utils

import play.api.mvc._

/**
  * Created by Hockie on 4/28/2016.
  */

sealed trait FlashMessage {
  val flash: Flash
}

case class MessageError(message: String) extends FlashMessage {
  override val flash: Flash = Flash(Map("error" -> message))
}

case class MessageSuccess(message: String) extends FlashMessage {
  override val flash: Flash = Flash(Map("success" -> message))
}
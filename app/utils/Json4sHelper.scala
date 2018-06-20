package utils

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime, ZonedDateTime}
import java.util.{Locale, TimeZone, UUID}
import org.json4s._
import utils.JsonDateTime._
import scala.language.implicitConversions

case object UUIDSerializer extends CustomSerializer[UUID](format => ( {
  case JString(s) => UUID.fromString(s)
}, {
  case x: UUID => JString(x.toString)
}
)
)

case object DateTimeSerializer extends CustomSerializer[LocalTime](format => ( {
  case JString(s) => LocalTime.parse(s)
}, {
  case x: LocalTime => JString(x.toString)
}
)
)

case object LocalDateSerializer extends CustomSerializer[LocalDate](format => ( {
  case JString(s) => LocalDate.parse(s, JsonLocalDateFormat)
}, {
  case x: LocalDate => JString(x.toString)
}
)
)

case object ZonedDateTimeSerializer extends CustomSerializer[ZonedDateTime](format => ( {
  case JString(s) => ZonedDateTime.parse(s, JsonDateFormat)
}, {
  case x: ZonedDateTime => JString(x.toJsonString)
}
)
)

object Json4sHelper {
  implicit val formats = DefaultFormats.preservingEmptyValues + UUIDSerializer + DateTimeSerializer + ZonedDateTimeSerializer + LocalDateSerializer
}

object JsonDateTime {
  val JsonDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  val JsonLocalDateFormat = DateTimeFormatter.ofPattern("dd/MM//yyyy").withLocale(Locale.US)

  implicit def convertZonedDateTimeToJsonDateTime(value: ZonedDateTime): ZoneDateTimeJson = new ZoneDateTimeJson(value)

  class ZoneDateTimeJson(z: ZonedDateTime) {
    def toJsonString: String = {
      val s = ZonedDateTime.ofInstant(z.toLocalDateTime, z.getOffset, TimeZone.getTimeZone("UTC").toZoneId)
      JsonDateFormat.format(s)
    }
  }

}

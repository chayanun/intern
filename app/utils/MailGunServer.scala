package utils

import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter
import com.sun.jersey.multipart.FormDataMultiPart
import com.sun.jersey.multipart.file.FileDataBodyPart
import com.typesafe.config.{Config, ConfigFactory}
import javax.inject._
import javax.ws.rs.core.MediaType
import play.api.{Environment, Logger}


class MailGunServer @Inject()(env: Environment) {
  val config: Config = ConfigFactory.load

  def send(mailTo: String, title: String, message: String): Unit = {
    val client = Client.create()
    client.addFilter(new HTTPBasicAuthFilter("api", config.getString("mailgun.api.key")))
    val webResource = client.resource(config.getString("mailgun.api.url"))
    val sender = config.getString("mailgun.default.sender")
    val logo = env.getFile("/public/images/simple_logo.jpg")
    val form = new FormDataMultiPart
    form.field("from",sender)
    form.field("to", mailTo)
    form.field("subject", title)
    form.field("html", message)
    //attach file
    form.bodyPart(new FileDataBodyPart("inline", logo, MediaType.APPLICATION_OCTET_STREAM_TYPE))

    try {
      webResource.`type`(MediaType.MULTIPART_FORM_DATA_TYPE).post(form)
      Logger.info("Email sent!")
    }catch {
      case e: Exception =>
        Logger.error(s"${e.getMessage}")
    }

  }
}

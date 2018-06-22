package utils

import javax.inject._
import play.api.Logger
import play.api.libs.mailer._

class MailServer @Inject()(mailerClient: MailerClient) {
  def send(mailTo: Seq[String], title: String, message: String) = {
    val email = Email(
      title,
      "Simple Blog <service@simple-blog.com>",
      mailTo,
      bodyHtml = Some(message)
    )

    try {
      mailerClient.send(email)
      Logger.info("Email sent!")
    }catch {
      case e: Exception =>
        Logger.error(s"${e.getMessage}")
    }

  }
}

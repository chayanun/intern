package utils

import java.util.Base64

object Helpers {
  def encodeBase64(str: String): String = {
    Base64.getEncoder.encodeToString(str.getBytes("UTF-8"))
  }

  def decodeBase64(str: String): String = {
    new String(Base64.getDecoder.decode(str), "UTF-8")
  }
}

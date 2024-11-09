package java.net

import java.nio.charset.StandardCharsets
import java.nio.charset.Charset

object URLEncoder {
  def encode(s: String, enc: String): String = {
    val charset = Charset.forName(enc)
    val bytes = s.getBytes(charset)
    var sb = ""

    for (b <- bytes) {
      val ch = b.toChar
      if (isUnreserved(ch)) {
        sb = sb + ch
      } else {
        sb + s"%${b & 0xFF}%02X"
      }
    }

    sb.toString()
  }

  private def isUnreserved(ch: Char): Boolean = {
    ch.isLetterOrDigit || "-_.~".contains(ch)
  }
}
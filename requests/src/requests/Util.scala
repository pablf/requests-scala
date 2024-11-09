package requests

import java.io.{FileInputStream, InputStream, OutputStream}
import java.net.URLEncoder

object Util {
  def transferTo(is: InputStream,
                 os: OutputStream,
                 bufferSize: Int = 8 * 1024) = {
    val buffer = new Array[Byte](bufferSize)
    while ( {
      is.read(buffer) match {
        case -1 => false
        case n =>
          os.write(buffer, 0, n)
          true
      }
    }) ()
  }

  def urlEncode(x: Iterable[(String, String)]) = {
    x.map{case (k, v) => URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")}
      .mkString("&")
  }

  def urlEncodeSingle(k: String, v: String): String = {
    URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")  
  }

}

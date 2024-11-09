package requests

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.util.zip.{GZIPInputStream, InflaterInputStream}
import scala.annotation.tailrec
import requests.Compress._

/** Stream uncompresser
  * @param c
  *   Compression mode
  */
class Plumper(c: Compress) {

  private def wrap(is: InputStream): InputStream =
    c match {
      case None    => is
      case Gzip    => new GZIPInputStream(is)
      case Deflate => new InflaterInputStream(is)
    }

  def decompress(compressed: InputStream): String = {
    val gis = wrap(compressed)
    val br = new BufferedReader(new InputStreamReader(gis, "UTF-8"))
    val sb = new StringBuilder()

    @tailrec
    def read(): Unit = {
      val line = br.readLine
      if (line != null) {
        sb.append(line)
        read()
      }
    }

    read()
    br.close()
    gis.close()
    compressed.close()
    sb.toString()
  }
}
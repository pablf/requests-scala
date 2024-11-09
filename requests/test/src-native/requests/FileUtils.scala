package requests

import java.io.InputStream
import java.io.FileInputStream
import javax.net.ssl.SSLContext

object FileUtils {

  def getFile(path: String): InputStream = {

  
    val currentRelativePath = java.nio.file.Paths.get("")
    val path = currentRelativePath
      .toAbsolutePath()
      .getParent()
      .getParent()
      .getParent()
      .getParent()
      .getParent()
      .resolve("requests/test/resources/license.zip")
    val file = path.toFile()
    var stream: InputStream = null
    try{
      stream = new FileInputStream(file)
    } catch {
      case e: Exception => {
        val path = currentRelativePath
          .toAbsolutePath()
          .getParent()
          .getParent()
          .getParent()
          .getParent()
          .resolve("requests/test/resources/license.zip")
        val file = path.toFile()
        stream = new FileInputStream(file)
      }
    }
    stream
  }
  def createSslContext(keyStorePath: String, keyStorePassword: String): SSLContext = 
    ???

}

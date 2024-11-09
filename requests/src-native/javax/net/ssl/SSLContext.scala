package javax.net.ssl

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import requests.internal.libcurl._
import requests.internal.CurlConstants._
import requests.internal.CurlOps._

case class SSLContext(
  certFile: String = "", 
  keyFile: String = "",
  caFile: String = "",
  engine: Option[String] = None,
  keyPassphrase: Option[String] = None,
  keyType: String = "PEM",
  keyName: String = "",
  verifyPeer: Boolean = true)
  
object SSLContext{

  def applyTo(ssl: SSLContext, handle: Ptr[CURL]): Unit = {
    Zone.acquire { implicit z =>
      val certFileCStr = toCString(ssl.certFile)
      val keyFileCStr = toCString(ssl.keyFile)
      val caFileCStr = toCString(ssl.caFile)
      val keyTypeCStr = toCString(ssl.keyType)
      val keyNameCStr = toCString(ssl.keyName)

      // Set the SSL certificate file
      if (handle.setOpt(CURLOPT_SSLCERT, certFileCStr) != CURLE_OK) {
        throw new RuntimeException("Failed to set SSL certificate file")
      }

      // Set the SSL key file
      if (handle.setOpt(CURLOPT_SSLKEY, keyFileCStr) != CURLE_OK) {
        throw new RuntimeException("Failed to set SSL key file")
      }

      // Set the CA certificate file
      if (handle.setOpt(CURLOPT_CAINFO, caFileCStr) != CURLE_OK) {
        throw new RuntimeException("Failed to set CA certificate file")
      }

      // Set the SSL engine if provided
      ssl.engine.foreach { eng =>
        val engineCStr = toCString(eng)
        if (handle.setOpt(CURLOPT_SSLENGINE, engineCStr) != CURLE_OK) {
          throw new RuntimeException("Failed to set SSL engine")
        }
        if (handle.setOpt(CURLOPT_SSLENGINE_DEFAULT, 1L) != CURLE_OK) {
          throw new RuntimeException("Failed to set SSL engine as default")
        }
      }

      // Set the key passphrase if provided
      ssl.keyPassphrase.foreach { passphrase =>
        val passphraseCStr = toCString(passphrase)
        if (handle.setOpt(CURLOPT_KEYPASSWD, passphraseCStr) != CURLE_OK) {
          throw new RuntimeException("Failed to set key passphrase")
        }
      }

      // Set the SSL key type
      if (handle.setOpt(CURLOPT_SSLKEYTYPE, keyTypeCStr) != CURLE_OK) {
        throw new RuntimeException("Failed to set SSL key type")
      }

      // Set the SSL key name
      if (handle.setOpt(CURLOPT_SSLKEY, keyNameCStr) != CURLE_OK) {
        throw new RuntimeException("Failed to set SSL key name")
      }

      // Set SSL verification of the peer's certificate
      if (handle.setOpt(CURLOPT_SSL_VERIFYPEER, if (ssl.verifyPeer) 1L else 0L) != CURLE_OK) {
        throw new RuntimeException("Failed to set SSL verify peer")
      }
    }
  }
}
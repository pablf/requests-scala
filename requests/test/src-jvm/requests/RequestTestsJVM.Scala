package requests

import utest._
import ujson._

object RequestTests extends TestSuite {

  val tests = Tests{

    test("failures"){
      intercept[java.net.MalformedURLException]{
        requests.get("://doesnt-exist.com/")
      }
    }

    test("clientCertificate"){
      val base = "./requests/test/resources"
      val url = "https://client.badssl.com"
      val instruction = "https://github.com/lihaoyi/requests-scala/blob/master/requests/test/resources/badssl.com-client.md"
      val certificateExpiredMessage = s"WARNING: Certificate may have expired and needs to be updated. Please check: $instruction and/or file issue"
      test("noPassword"){
        val res = requests.get(
          "https://client.badssl.com",
          cert = Cert.implicitP12(s"$base/badssl.com-client-nopass.p12"),
          check = false
        )
        if (res.statusCode == 400)
          println(certificateExpiredMessage)
        else
          assert(res.statusCode == 200)
      }
      test("sslContext"){
        val res = requests.get(
          "https://client.badssl.com",
          sslContext = FileUtils.createSslContext(s"$base/badssl.com-client.p12", "badssl.com"),
          check = false
        )
        if (res.statusCode == 400)
          println(certificateExpiredMessage)
        else
          assert(res.statusCode == 200)
      }
    }
  }
}

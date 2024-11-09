package requests

import utest._

abstract class HttpbinTestSuite extends TestSuite.Retries {
  override val utestRetryCount = 3
  val localHttpbin: String = "httpbin.org"

}

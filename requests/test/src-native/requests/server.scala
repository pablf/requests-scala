package requests

import scala.scalanative.unsafe._
import requests.libmicrohttpd._

@extern @link("microhttpd") @link("z") @define("REQUESTS_SCALA_TEST_SERVER")  
object server {

  def start(): Ptr[MHD_Daemon] = extern
  def port(): CInt = extern
  def stop(daemon: Ptr[MHD_Daemon]): Unit = extern

}

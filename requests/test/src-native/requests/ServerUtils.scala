package requests

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.libc.stdlib._
import scala.scalanative.libc.string._
import libmicrohttpd._
import libmicrohttpdConstants._
import requests.Compress._
import java.io.ByteArrayInputStream
import requests.server

object ServerUtils {
  def usingEchoServer(f: Int => Unit): Unit = Zone.acquire { implicit z =>
    val s = EchoServer()
    try {
      s.start()
      f(s.getPort())
    }
    finally s.stop()
  }

  private case class EchoServer() {
    private var daemon: Ptr[MHD_Daemon] = null

    def start() = {
      daemon = server.start()
    }

    def getPort(): Int = server.port()

    def stop(): Unit = {
      if (daemon != null) {
        server.stop(daemon)
      }
    }
  }
}
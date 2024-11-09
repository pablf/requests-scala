package requests.internal

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import libcurl._
import CurlConstants._

object CurlOps {
  implicit class CurlEasyOps(private val handle: Ptr[CURL]) {
    def cleanup: Unit = 
      libcurl.curl_easy_cleanup(handle)

    def perform: CInt = 
      libcurl.curl_easy_perform(handle)

    def pause(v: CInt): CInt = 
      libcurl.curl_easy_pause(handle, v)

    def unpause: CurlECode = 
        pause(CURLPAUSE_CONT)

        
    def setOpt(option: CurlOption, param: Int): CurlECode =
      libcurl.setoptInt(handle, option, param)
    
    def setOpt(option: CurlOption, param: Long): CurlECode =
      libcurl.setoptLong(handle, option, param)

    def setOpt(option: CurlOption, param: String)(implicit zone: Zone): CurlECode =
      libcurl.setoptPtr(handle, option, toCString(param))

    def setOpt(option: CurlOption, param: Boolean): CurlECode =
      libcurl.setoptInt(handle, option, if (param) 1 else 0)

    def setOpt(option: CurlOption): CurlECode =
      libcurl.setoptInt(handle, option, 1)

    def setOpt(option: CurlOption, param: CVoidPtr): CurlECode =
      libcurl.setoptPtr(handle, option, param)

    def setOpt(option: CurlOption, param: CFuncPtr): CurlECode =
      setOpt(option, param)

    def status: Int = Zone.acquire { implicit z =>
      val status = alloc[Long](1)
      val res = getinfo(CURLINFO_RESPONSE_CODE, status)
      if (res == CURLE_OK) {
        (!status).toInt
      } else {
        throw new Exception(s"Cannot obtain status code: ${res}")
      }
    }
    
    def getinfo(info: CInt, params: Ptr[_]): CInt = libcurl.curl_easy_getinfo(handle, info, params)
  }

  implicit class CurlMultiOps(private val handle: Ptr[CURLM]) {
    
    def cleanup: Unit = libcurl.curl_multi_cleanup(handle)

    def add(easy: Ptr[CURL]): CInt = libcurl.curl_multi_add_handle(handle, easy)

    def remove(easy: Ptr[CURL]): CInt = libcurl.curl_multi_remove_handle(handle, easy)

    def perform(runningHandles: Ptr[Int]): CInt = libcurl.curl_multi_perform(handle, runningHandles)

    def info_read(msgsInQueue: Ptr[Int]): Ptr[CurlMsg] = libcurl.curl_multi_info_read(handle, msgsInQueue)

    def poll(timeoutMs: Int): CInt =
      libcurl.curl_multi_poll(handle, null, 0.toUInt, timeoutMs, null)

    def poll(timeoutMs: Int, numfds: Ptr[Int]): CInt =
      libcurl.curl_multi_poll(handle, null, 0.toUInt, timeoutMs, numfds)

    def wait(timeoutMs: Int): CInt =
      libcurl.curl_multi_wait(handle, null, 0.toUInt, timeoutMs, null)

    def wait(extra_fds: Ptr[_], timeoutMs: Int, numfds: Ptr[Int]): CInt =
      libcurl.curl_multi_wait(handle, null, 0.toUInt, timeoutMs, numfds)

  }

  implicit class CurlUrlOps(private val handle: Ptr[CURLU]) {
    
    def cleanup: Unit = libcurl.curl_url_cleanup(handle)

    def set(part: CInt, content: String, flags: Int)(implicit zone: Zone): CInt = 
      libcurl.curl_url_set(handle, part, toCString(content), flags.toUInt)

    //def get(part: CInt, content: Ptr[Ptr[Byte]], flags: Int): CInt = libcurl.curl_url_get(handle, part, content, flags.toUInt)
    //def get(part: CInt, content: Ptr[Ptr[Byte]], flags: Int): Option[String] = libcurl.curl_url_get(handle, part, content, flags.toUInt)
    def get(part: CInt): Option[String] = Zone.acquire { implicit z =>
        val pt: Ptr[Ptr[Byte]] = alloc(1)
        val status = libcurl.curl_url_get(handle, part, pt, 0.toUInt)
        if (status == CURLE_OK) {
          Some(if (pt == null) "" else fromCString(!pt))
        } else None
      }
      
      
      
  }

  implicit class CurlSlistOps(private val handle: Ptr[CurlSlist]) {
    
    def append(string: String)(implicit zone: Zone): Ptr[CurlSlist] = libcurl.curl_slist_append(handle, toCString(string))

    def freeAll: Unit = libcurl.curl_slist_free_all(handle)
  }
}





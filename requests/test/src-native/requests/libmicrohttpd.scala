package requests

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@extern @link("microhttpd")
object libmicrohttpd {
  type MHD_Daemon
  type MHD_Connection
  type MHD_Response
  type MHD_Result
  type MHD_AcceptPolicyCallback = CFuncPtr2[Ptr[Byte], Ptr[Byte], CInt]
  type MHD_NotifyConnectionCallback = CFuncPtr4[Ptr[_], Ptr[MHD_Connection], CInt, Any, Unit]
  type MHD_AccessHandlerCallback = CFuncPtr8[
    Ptr[_], // cls
    Ptr[MHD_Connection], // connection
    CString, // url
    CString, // method
    CString, // version
    CString, // upload_data
    Ptr[CSize], // upload_data_size
    Ptr[Ptr[_]], // con_cls
    MHD_Result // return type
  ]
  def MHD_start_daemon(flags: CUnsignedInt, port: CUnsignedShort, apc: MHD_AcceptPolicyCallback, apc_cls: Ptr[_], dh: MHD_AccessHandlerCallback, dh_cls: Ptr[_], opt: Any*): Ptr[MHD_Daemon] = extern
  def MHD_stop_daemon(daemon: Ptr[MHD_Daemon]): Unit = extern
  def MHD_create_response_from_buffer(size: CSize, buffer: Ptr[Byte], mode: UInt): Ptr[MHD_Response] = extern
  def MHD_queue_response(connection: Ptr[MHD_Connection], status_code: UInt, response: Ptr[MHD_Response]): MHD_Result = extern
  def MHD_destroy_response(response: Ptr[MHD_Response]): Unit = extern
  def MHD_lookup_connection_value(connection: Ptr[MHD_Connection], kind: CInt, key: CString): CString = extern

}

object libmicrohttpdConstants {
  val MHD_USE_THREAD_PER_CONNECTION: UInt = 1.toUInt
  val MHD_RESPMEM_MUST_COPY: UInt = 2.toUInt
  val MHD_YES: CInt = 1
  val MHD_OPTION_END: UInt = 0.toUInt
  val MHD_OPTION_CONNECTION_LIMIT = 2.toUInt
  val MHD_OPTION_CONNECTION_TIMEOUT = 3.toUInt
  val MHD_HEADER_KIND: CInt = 1
  val MHD_OPTION_NOTIFY_CONNECTION = 27.toUInt
}
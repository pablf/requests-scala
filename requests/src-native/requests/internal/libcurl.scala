package requests.internal

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@link("curl") @extern @define("REQUESTS_SCALA_AUX")  
object libcurl {

  type CURL
  type CURLM
  type CURLU
  type CurlSlist

  type CurlMsg = CStruct3[
    CInt,
    Ptr[CURL],
    Ptr[_]
  ]

    @name("requests_scala_curl_setopt_int")
  def setoptInt(handle: Ptr[CURL], option: CInt, parameter: Int): CInt = extern

  @name("requests_scala_curl_setopt_long")
  def setoptLong(handle: Ptr[CURL], option: CInt, parameter: Long): CInt = extern

  @name("requests_scala_curl_setopt_pointer")
  def setoptPtr(handle: Ptr[CURL], option: CInt, parameter: Ptr[_]): CInt = extern

  def curl_easy_setoptG[A](handle: Ptr[CURL], option: CInt, parameter: Ptr[A]): CInt = extern

  def curl_easy_init(): Ptr[CURL] = extern

  def curl_easy_cleanup(handle: Ptr[CURL]): Unit = extern

  def curl_easy_perform(easy_handle: Ptr[CURL]): CInt = extern

  def curl_easy_strerror(errCode: CInt): CString = extern

  def curl_easy_pause(easy_handle: Ptr[CURL], bitmask: CInt): CInt = extern

  def curl_easy_getinfo(easy_handle: Ptr[CURL], info: CInt, params: Ptr[_]): CInt = extern

  def curl_url(): Ptr[CURLU] = extern

  def curl_url_cleanup(url: Ptr[CURLU]): Unit = extern

  def curl_url_set(url: Ptr[CURLU], part: CInt, content: CString, flags: UInt): CInt = extern 

  def curl_url_get(url: Ptr[CURLU], part: CInt, content: Ptr[Ptr[Byte]], flags: UInt): CInt = extern

  def curl_slist_append(list: Ptr[CurlSlist], string: CString): Ptr[CurlSlist] = extern

  def curl_slist_free_all(list: Ptr[CurlSlist]): Unit = extern

  def curl_global_init(flags: CLong): CInt = extern

  def curl_multi_init(): Ptr[CURLM] = extern
  
  def curl_multi_cleanup(multi: Ptr[CURLM]): Unit = extern

  def curl_multi_add_handle(multi: Ptr[CURLM], easy: Ptr[CURL]): CInt = extern

  def curl_multi_remove_handle(multi: Ptr[CURLM], easy: Ptr[CURL]): CInt = extern

  def curl_multi_perform(multi: Ptr[CURLM], runningHandles: Ptr[CInt]): CInt = extern

  def curl_multi_info_read(multi: Ptr[CURLM], msgsInQueue: Ptr[Int]): Ptr[CurlMsg] = extern

  def curl_multi_poll(multi: Ptr[CURLM], extra_fds: Ptr[Byte], extra_nfds: UInt, timeoutMs: CInt, numfds: Ptr[CInt]): CInt = extern

  def curl_multi_wait(multi: Ptr[CURLM], extra_fds: Ptr[Byte], extra_nfds: UInt, timeoutMs: CInt, numfds: Ptr[CInt]): CInt = extern

}

object CurlConstants {

  type CurlOption = CInt

  type CurlInfo = CInt
  type CurlECode = CInt
  type CurlMCode = CInt
  type CurlUCode = CInt

  val CURLOPT_VERBOSE = 41
  val CURLOPT_PROXY = 10004
  val CURLOPT_SSL_VERIFYHOST = 81
  val CURLOPT_SSL_VERIFYPEER = 64
  val CURLOPT_SSLCERTTYPE = 10086
  val CURLOPT_SSLCERT = 10025
  val CURLOPT_SSLKEY = 10087
  val CURLOPT_CAINFO = 10065
  val CURLOPT_SSLENGINE = 10089
  val CURLOPT_SSLENGINE_DEFAULT = 90
  val CURLOPT_SSLKEYTYPE = 10088
  val CURLOPT_SSLVERSION = 32
  val CURLOPT_KEYPASSWD = 10026
  val CURLOPT_FOLLOWLOCATION = 52
  val CURLOPT_HTTPGET = 80
  val CURLOPT_NOBODY = 44
  val CURLOPT_POST = 47
  val CURLOPT_RTSP_REQUEST = 189
  val CURLOPT_CONNECT_ONLY = 141
  val CURLOPT_CUSTOMREQUEST = 10036
  val CURLOPT_HTTPHEADER = 10023
  val CURLOPT_CONNECTTIMEOUT_MS = 156
  val CURLOPT_TIMEOUT_MS = 155
  val CURLOPT_ERRORBUFFER = 10010
  val CURLOPT_MAXREDIRS = 68
  val CURLOPT_URL = 10002
  val CURLOPT_HEADERDATA = 10029
  val CURLOPT_HEADERFUNCTION = 20079
  val CURLOPT_WRITEDATA = 10001
  val CURLOPT_WRITEFUNCTION = 20011
  val CURLOPT_READDATA = 10009
  val CURLOPT_READFUNCTION = 20012
  val CURLOPT_INFILESIZE = 14
  val CURLOPT_POSTFIELDSIZE = 60
  val CURLOPT_UPLOAD = 46

  // CURLINFO constants
  val CURLINFO_EFFECTIVE_URL = 1048577
  val CURLINFO_RESPONSE_CODE = 2097154
  val CURLINFO_TOTAL_TIME = 3145731
  val CURLINFO_NAMELOOKUP_TIME = 3145732
  val CURLINFO_CONNECT_TIME = 3145733
  val CURLINFO_PRETRANSFER_TIME = 3145734
  val CURLINFO_SIZE_UPLOAD = 3145735
  val CURLINFO_SIZE_UPLOAD_T = 6291463
  val CURLINFO_SIZE_DOWNLOAD = 3145736
  val CURLINFO_SIZE_DOWNLOAD_T = 6291464
  val CURLINFO_SPEED_DOWNLOAD = 3145737
  val CURLINFO_SPEED_DOWNLOAD_T = 6291465
  val CURLINFO_SPEED_UPLOAD = 3145738
  val CURLINFO_SPEED_UPLOAD_T = 6291466
  val CURLINFO_HEADER_SIZE = 2097163
  val CURLINFO_REQUEST_SIZE = 2097164
  val CURLINFO_SSL_VERIFYRESULT = 2097165
  val CURLINFO_FILETIME = 2097166
  val CURLINFO_CONTENT_LENGTH_DOWNLOAD = 3145743
  val CURLINFO_CONTENT_LENGTH_DOWNLOAD_T = 6291471
  val CURLINFO_CONTENT_LENGTH_UPLOAD = 3145744
  val CURLINFO_CONTENT_LENGTH_UPLOAD_T = 6291472
  val CURLINFO_CONTENT_TYPE = 1048594
  val CURLINFO_REDIRECT_TIME = 3145747
  val CURLINFO_REDIRECT_COUNT = 2097172
  val CURLINFO_PRIVATE = 1048597
  val CURLINFO_HTTP_CONNECTCODE = 2097174
  val CURLINFO_HTTPAUTH_AVAIL = 2097175
  val CURLINFO_PROXYAUTH_AVAIL = 2097176
  val CURLINFO_OS_ERRNO = 2097177
  val CURLINFO_NUM_CONNECTS = 2097178
  val CURLINFO_SSL_ENGINES = 4194331
  val CURLINFO_COOKIELIST = 4194332
  val CURLINFO_LASTSOCKET = 2097181
  val CURLINFO_FTP_ENTRY_PATH = 1048606
  val CURLINFO_REDIRECT_URL = 1048607
  val CURLINFO_PRIMARY_IP = 1048608
  val CURLINFO_APPCONNECT_TIME = 3145761
  val CURLINFO_CERTINFO = 4194338
  val CURLINFO_CONDITION_UNMET = 2097187
  val CURLINFO_RTSP_SESSION_ID = 1048612
  val CURLINFO_RTSP_CLIENT_CSEQ = 2097189
  val CURLINFO_RTSP_SERVER_CSEQ = 2097190
  val CURLINFO_RTSP_CSEQ_RECV = 2097191
  val CURLINFO_PRIMARY_PORT = 2097192
  val CURLINFO_LOCAL_IP = 1048617
  val CURLINFO_LOCAL_PORT = 2097194
  val CURLINFO_TLS_SESSION = 4194345
  val CURLINFO_ACTIVESOCKET = 5242926

  // CURLcode constants
  val CURLE_OK = 0
  val CURLE_UNSUPPORTED_PROTOCOL = 1
  val CURLE_FAILED_INIT = 2
  val CURLE_URL_MALFORMAT = 3
  val CURLE_NOT_BUILT_IN = 4
  val CURLE_COULDNT_RESOLVE_PROXY = 5
  val CURLE_COULDNT_RESOLVE_HOST = 6
  val CURLE_COULDNT_CONNECT = 7
  val CURLE_WEIRD_SERVER_REPLY = 8
  val CURLE_REMOTE_ACCESS_DENIED = 9
  val CURLE_FTP_ACCEPT_FAILED = 10
  val CURLE_FTP_WEIRD_PASS_REPLY = 11
  val CURLE_FTP_ACCEPT_TIMEOUT = 12
  val CURLE_FTP_WEIRD_PASV_REPLY = 13
  val CURLE_FTP_WEIRD_227_FORMAT = 14
  val CURLE_FTP_CANT_GET_HOST = 15
  val CURLE_HTTP2 = 16
  val CURLE_FTP_COULDNT_SET_TYPE = 17
  val CURLE_PARTIAL_FILE = 18
  val CURLE_FTP_COULDNT_RETR_FILE = 19
  val CURLE_QUOTE_ERROR = 21
  val CURLE_HTTP_RETURNED_ERROR = 22
  val CURLE_WRITE_ERROR = 23
  val CURLE_UPLOAD_FAILED = 25
  val CURLE_READ_ERROR = 26
  val CURLE_OUT_OF_MEMORY = 27
  val CURLE_OPERATION_TIMEDOUT = 28
  val CURLE_FTP_PORT_FAILED = 30
  val CURLE_FTP_COULDNT_USE_REST = 31
  val CURLE_RANGE_ERROR = 33
  val CURLE_HTTP_POST_ERROR = 34
  val CURLE_SSL_CONNECT_ERROR = 35
  val CURLE_BAD_DOWNLOAD_RESUME = 36
  val CURLE_FILE_COULDNT_READ_FILE = 37
  val CURLE_LDAP_CANNOT_BIND = 38
  val CURLE_LDAP_SEARCH_FAILED = 39
  val CURLE_FUNCTION_NOT_FOUND = 41
  val CURLE_ABORTED_BY_CALLBACK = 42
  val CURLE_BAD_FUNCTION_ARGUMENT = 43
  val CURLE_INTERFACE_FAILED = 45
  val CURLE_TOO_MANY_REDIRECTS = 47
  val CURLE_UNKNOWN_OPTION = 48
  val CURLE_TELNET_OPTION_SYNTAX = 49
  val CURLE_GOT_NOTHING = 52
  val CURLE_SSL_ENGINE_NOTFOUND = 53
  val CURLE_SSL_ENGINE_SETFAILED = 54
  val CURLE_SEND_ERROR = 55
  val CURLE_RECV_ERROR = 56
  val CURLE_SSL_CERTPROBLEM = 58
  val CURLE_SSL_CIPHER = 59
  val CURLE_PEER_FAILED_VERIFICATION = 60
  val CURLE_BAD_CONTENT_ENCODING = 61
  val CURLE_LDAP_INVALID_URL = 62
  val CURLE_FILESIZE_EXCEEDED = 63
  val CURLE_USE_SSL_FAILED = 64
  val CURLE_SEND_FAIL_REWIND = 65
  val CURLE_SSL_ENGINE_INITFAILED = 66
  val CURLE_LOGIN_DENIED = 67
  val CURLE_TFTP_NOTFOUND = 68
  val CURLE_TFTP_PERM = 69
  val CURLE_REMOTE_DISK_FULL = 70
  val CURLE_TFTP_ILLEGAL = 71
  val CURLE_TFTP_UNKNOWNID = 72
  val CURLE_REMOTE_FILE_EXISTS = 73
  val CURLE_TFTP_NOSUCHUSER = 74
  val CURLE_CONV_FAILED = 75
  val CURLE_CONV_REQD = 76
  val CURLE_SSL_CACERT_BADFILE = 77
  val CURLE_REMOTE_FILE_NOT_FOUND = 78
  val CURLE_SSH = 79
  val CURLE_SSL_SHUTDOWN_FAILED = 80
  val CURLE_AGAIN = 81
  val CURLE_SSL_CRL_BADFILE = 82
  val CURLE_SSL_ISSUER_ERROR = 83
  val CURLE_FTP_PRET_FAILED = 84
  val CURLE_RTSP_CSEQ_ERROR = 85
  val CURLE_RTSP_SESSION_ERROR = 86
  val CURLE_FTP_BAD_FILE_LIST = 87
  val CURLE_CHUNK_FAILED = 88

  // CURLUPART constants
  val CURLUPART_URL = 0
  val CURLUPART_SCHEME = 1
  val CURLUPART_USER = 2
  val CURLUPART_PASSWORD = 3
  val CURLUPART_OPTIONS = 4
  val CURLUPART_HOST = 5
  val CURLUPART_PORT = 6
  val CURLUPART_PATH = 7
  val CURLUPART_QUERY = 8
  val CURLUPART_FRAGMENT = 9
  val CURLUPART_ZONEID = 10

  // CURLU flags
  val CURLU_DEFAULT_PORT = (1 << 0)
  val CURLU_NO_DEFAULT_PORT = (1 << 1)
  val CURLU_DEFAULT_SCHEME = (1 << 2)
  val CURLU_NO_DEFAULT_SCHEME = (1 << 3)
  val CURLU_PATH_AS_IS = (1 << 4)
  val CURLU_DISALLOW_USER = (1 << 5)
  val CURLU_URLDECODE = (1 << 6)
  val CURLU_URLENCODE = (1 << 7)
  val CURLU_APPENDQUERY = (1 << 8)
  val CURLU_GUESS_SCHEME = (1 << 9)
  val CURLU_NO_AUTHORITY = (1 << 10)
  val CURLU_ALLOW_SPACE = (1 << 11)


  // CURLUcode constants
  val CURLUE_OK = 0
  val CURLUE_BAD_HANDLE = 1
  val CURLUE_BAD_PARTPOINTER = 2
  val CURLUE_MALFORMED_INPUT = 3
  val CURLUE_BAD_PORT_NUMBER = 4
  val CURLUE_UNSUPPORTED_SCHEME = 5
  val CURLUE_URLDECODE = 6
  val CURLUE_OUT_OF_MEMORY = 7
  val CURLUE_USER_NOT_ALLOWED = 8
  val CURLUE_UNKNOWN_PART = 9
  val CURLUE_NO_SCHEME = 10
  val CURLUE_NO_USER = 11
  val CURLUE_NO_PASSWORD = 12
  val CURLUE_NO_OPTIONS = 13
  val CURLUE_NO_HOST = 14
  val CURLUE_NO_PORT = 15
  val CURLUE_NO_QUERY = 16
  val CURLUE_NO_FRAGMENT = 17

  // CURLMcode constants
  val CURLM_CALL_MULTI_PERFORM = -1
  val CURLM_OK = 0
  val CURLM_BAD_HANDLE = 1
  val CURLM_BAD_EASY_HANDLE = 2
  val CURLM_OUT_OF_MEMORY = 3
  val CURLM_INTERNAL_ERROR = 4
  val CURLM_BAD_SOCKET = 5
  val CURLM_UNKNOWN_OPTION = 6
  val CURLM_ADDED_ALREADY = 7

  // CURLPAUSEFLAG constants
  val CURLPAUSE_RECV = (1 << 0)
  val CURLPAUSE_RECV_CONT = 0
  val CURLPAUSE_SEND = 1
  val CURLPAUSE_SEND_CONT = 0
  val CURLPAUSE_ALL = 3
  val CURLPAUSE_CONT = 0

  val CURL_WRITEFUNC_PAUSE = 0x10000001
  val CURL_READFUNC_PAUSE  = 0x10000001




}
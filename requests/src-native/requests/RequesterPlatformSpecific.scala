package requests

import javax.net.ssl.SSLContext
import java.net.HttpCookie
import java.io.InputStream
import scala.scalanative.unsafe._
import scala.scalanative.runtime
import scala.scalanative.runtime.Intrinsics
import scala.scalanative.unsigned._
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import scala.scalanative.runtime.ffi
import internal.libcurl._
import internal.CurlConstants._
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import requests.internal.libcurl
import requests.internal.CurlConstants._
import requests.internal.CurlOps._


trait RequesterPlatformSpecific {

  private def toPtr(a: AnyRef): Ptr[Byte] =
    runtime.fromRawPtr(Intrinsics.castObjectToRawPtr(a))

  protected def stream_(verb: String, 
            sess: BaseSession,
             url: String,
             auth: RequestAuth,
             params: Iterable[(String, String)],
             blobHeaders: Iterable[(String, String)],
             headers: Iterable[(String, String)],
             data: RequestBlob,
             readTimeout: Int,
             connectTimeout: Int,
             proxy: (String, Int),
             cert: Cert,
             sslContext: SSLContext,
             cookies: Map[String, HttpCookie],
             cookieValues: Map[String, String],
             maxRedirects: Int,
             verifySslCerts: Boolean,
             autoDecompress: Boolean,
             compress: Compress,
             keepAlive: Boolean,
             check: Boolean,
             chunkedUpload: Boolean,
             redirectedFrom: Option[Response],
             onHeadersReceived: StreamHeaders => Unit): geny.Readable = new geny.Readable {

    private val upperCaseVerb = verb.toUpperCase()
    def readBytesThrough[T](f: InputStream => T): T = {


      Zone.acquire { implicit z =>

        //initialize curl
        val handle = libcurl.curl_easy_init()
        lazy val multi = libcurl.curl_multi_init()
        //handle.setOpt(CURLOPT_VERBOSE)

        if (handle == null) throw new Exception("Failed to initialise curl")


        val curlu = libcurl.curl_url()

        if (curlu == null) throw new Exception("Failed to allocate URL; insufficient memory")

        var headersSlist: Ptr[CurlSlist] = null

        // set configuration
        checkCURLU(curlu.set(CURLUPART_URL, url, CURLU_URLENCODE))

        params.foreach {
          case (k, v) => 
            checkCURLU(curlu.set(CURLUPART_QUERY, Util.urlEncodeSingle(k, v), CURLU_APPENDQUERY))
        }
        
        if (proxy != null)
          checkCURL(handle.setOpt(CURLOPT_PROXY, s"${proxy._1}:${proxy._2}"), "Failed to set proxy")

        if (!verifySslCerts) {
          checkCURL(handle.setOpt(CURLOPT_SSL_VERIFYHOST, 0L), "Failed to set verify host")
          checkCURL(handle.setOpt(CURLOPT_SSL_VERIFYPEER, 0L), "Failed to set verify peer")
        } else {
          if (cert != null)
            cert match {
              case Cert.P12(p12, pwd) => 
                checkCURL(handle.setOpt(CURLOPT_SSLCERTTYPE, "P12"), "Failed to set cert type")
                checkCURL(handle.setOpt(CURLOPT_SSLCERT, p12), "Failed to set cert")
                pwd.map(p => checkCURL(handle.setOpt(CURLOPT_KEYPASSWD, p), "Failed to set cert password"))
              case _ => throw new Exception("Impossible error")
            } else if (sslContext != null) {
              javax.net.ssl.SSLContext.applyTo(sslContext, handle)
            }
        }

        checkCURL(handle.setOpt(CURLOPT_FOLLOWLOCATION), "Failed to set redirect follow")

        checkCURL(upperCaseVerb match {
          case "GET" => handle.setOpt(CURLOPT_HTTPGET)
          case "HEAD" => handle.setOpt(CURLOPT_NOBODY)
          case "POST" => handle.setOpt(CURLOPT_POST)
          case "OPTIONS" => handle.setOpt(CURLOPT_RTSP_REQUEST)
          case "CONNECT" => handle.setOpt(CURLOPT_CONNECT_ONLY)
          case other => handle.setOpt(CURLOPT_CUSTOMREQUEST, other)
        }, "Failed to set method")

        val sessionCookieValues = for{
          c <- (sess.cookies ++ cookies).valuesIterator
          if !c.hasExpired()
          if c.getDomain() == null || c.getDomain() == curlu.get(CURLUPART_HOST).getOrElse("")
          if c.getPath() == null || curlu.get(CURLUPART_PATH).map(_.startsWith(c.getPath())).getOrElse(false)
        } yield (c.getName(), c.getValue())

        val allCookies = sessionCookieValues ++ cookieValues

        val (contentLengthHeader, otherBlobHeaders) = blobHeaders.partition(_._1.equalsIgnoreCase("Content-Length"))

        val allHeaders =
          otherBlobHeaders ++
            sess.headers ++
            headers ++
            compress.headers ++
            auth.header.map("Authorization" -> _) ++
            (if (allCookies.isEmpty) None
            else Some("Cookie" -> allCookies
              .map { case (k, v) => s"""$k="$v"""" }
              .mkString("; ")
            ))

        val lastOfEachHeader =
          allHeaders.foldLeft(Map.empty[String, String]) {
            case (acc, (k, v)) =>
              acc.updated(k.toLowerCase, v)
          }
        lastOfEachHeader.foreach {case (k, v) => 
          headersSlist = appendHeader(headersSlist, k, v)
        }
                  if (chunkedUpload) {
          headersSlist = appendHeader(headersSlist, "Transfer-Encoding", "chunked")
          

        
        }

        if (upperCaseVerb == "PUT") {
          checkCURL(handle.setOpt(CURLOPT_UPLOAD), "Failed to set upload mode")
        }
        
        checkCURL(handle.setOpt(CURLOPT_HTTPHEADER, headersSlist), "Failed to set headers")
        checkCURL(handle.setOpt(CURLOPT_CONNECTTIMEOUT_MS, connectTimeout), "Failed to set connect timeout")
        checkCURL(handle.setOpt(CURLOPT_TIMEOUT_MS, readTimeout), "Failed to set timeout")
        val errBuf = alloc[CChar](256)
        checkCURL(handle.setOpt(CURLOPT_ERRORBUFFER, errBuf), "Failed to add error buffer")
        checkCURL(handle.setOpt(CURLOPT_MAXREDIRS, maxRedirects), "Failed to set max redirects")
        checkCURL(handle.setOpt(CURLOPT_URL, curlu.get(CURLUPART_URL).getOrElse("")), "Failed to set url")

        // Headers callback
        class HeaderData(var data: Map[String, Seq[String]] = Map.empty, var name: String = "", var content: String = "")
        val headerData = new HeaderData()
        
        val headerFunction = CFuncPtr.toPtr(CFuncPtr4.fromScalaFunction[Ptr[Byte], CSize, CSize, Ptr[Byte], CSize]{
          case (buffer, size, nitems, userdata) => {
            val headers = Intrinsics.castRawPtrToObject(runtime.toRawPtr(userdata)).asInstanceOf[HeaderData]
            val arr = new Array[Byte]((nitems * size).toInt)
            ffi.memcpy(arr.at(0), buffer, nitems * size)
            val str = new String(arr, StandardCharsets.UTF_8)
            
            if (!str.startsWith("HTTP")) {
              if(str.startsWith(" ") || str.startsWith("\t")) {
                //multi-line header
                headers.content = headers.content + str.trim()
              } else {
                // new header
                if (!headers.name.isEmpty) {
                  headers.data = headers.data.updated(headers.name, headers.data.getOrElse(headers.name, Nil) :+ headers.content)
                  headers.content = ""
                }
                val (name, content) = str.splitAt(str.indexOf(':'))
                headers.name = name
                headers.content = content.drop(1).trim()
              }
            }

            nitems * size
          }
        })

        checkCURL(handle.setOpt(CURLOPT_HEADERDATA, toPtr(headerData)), "Failed to set header callback's data")
        checkCURL(handle.setOpt(CURLOPT_HEADERFUNCTION, headerFunction), "Failed to set header callback")

        // Receiving body callback
        val input = new PipedInputStream()
        val piped = new PipedOutputStream(input)
        class WritingData(var fstBody: Boolean, var writer: PipedOutputStream)
        val writingData = new WritingData(false, piped)

        val writeFunction = CFuncPtr.toPtr(CFuncPtr4.fromScalaFunction[Ptr[Byte], CSize, CSize, Ptr[Byte], CSize]{
          (buffer, size, nitems, userdata) =>
            val totalSize = (nitems * size).toInt
            val writer = Intrinsics.castRawPtrToObject(runtime.toRawPtr(userdata)).asInstanceOf[WritingData]
            if (!writer.fstBody) {
              writer.fstBody = true
              CURL_WRITEFUNC_PAUSE.toCSize
            } else {
              val arr = new Array[Byte]((nitems * size).toInt)
              ffi.memcpy(arr.at(0), buffer, nitems * size)
              writer.writer.write(arr, 0, totalSize)
              writer.writer.flush()
              
              nitems * size
            }
        })

        checkCURL(handle.setOpt(CURLOPT_WRITEDATA, toPtr(writingData)), "Failed to set write callback's data")
        checkCURL(handle.setOpt(CURLOPT_WRITEFUNCTION, writeFunction), "Failed to set write callback")

        // Sending body callback
        val requestBodyInputStream = new PipedInputStream()
        val requestBodyOutputStream = new PipedOutputStream(requestBodyInputStream)
        usingOutputStream(compress.wrap(requestBodyOutputStream)) { os => data.write(os) }
        
        val readFunction: CVoidPtr = CFuncPtr.toPtr(CFuncPtr4.fromScalaFunction[Ptr[Byte], CSize, CSize, Ptr[Byte], CSize]{
          (buffer: Ptr[Byte], size: CSize, nitems: CSize, userdata: Ptr[Byte]) =>
            val reader = Intrinsics.castRawPtrToObject(runtime.toRawPtr(userdata)).asInstanceOf[PipedInputStream]
            val arr = new Array[Byte]((nitems * size).toInt)
            val length = reader.read(arr)
            if (length != -1) {
              ffi.memcpy(buffer, arr.at(0), length.toCSize)
              length.toCSize
            } else 0.toCSize 
        })

        checkCURL(handle.setOpt(CURLOPT_READDATA, toPtr(requestBodyInputStream)), "Failed to set read callback's data")
        checkCURL(handle.setOpt(CURLOPT_READFUNCTION, readFunction), "Failed to set read callback")

        // Start sending and receive only headers
        checkCURLM(multi.add(handle), "Failed to add handle to multi")
        var stillRunning = -1
        var continue = true
        var responseCode = 0
        

        while (stillRunning != 0 && continue && (responseCode == 0 || responseCode == 100 || responseCode == 302)) {

          val continueRunning = alloc[Int](1)
          val code = multi.perform(continueRunning)
          stillRunning = !continueRunning
          if (responseCode == 0 || responseCode == 100 || responseCode == 302) responseCode = handle.status
          if (code != CURLM_OK) {
            continue = false
          } else if ((stillRunning) != 0) {
            if (multi.poll(1000) != CURLM_OK) continue = false
          }
        }

        def clean() = {
          piped.close()
          multi.remove(handle)
          curlu.cleanup
          handle.cleanup
          multi.cleanup
          headersSlist.freeAll
        }

        if (responseCode == 0) {
          // bad transfer
          clean()
          val msg = scalanative.unsafe.fromCString(errBuf)
          if (msg.contains("time")) {
            throw new TimeoutException(url, readTimeout, connectTimeout)
          } else if (msg.contains("SSL")){
            throw new InvalidCertException(url, null)
          } else if (msg.contains("host")) {
            throw new UnknownHostException(url, msg)
          } else {
            throw new RequestsException(msg, None)
          } 
        } else {
          // good transfer
          // Start a thread to read the body and allow streaming contents
          val t = new Thread(new Runnable {
          def run(): Unit = {
            stillRunning = -1
            var continue = true
            handle.unpause
            while (stillRunning != 0 && continue) {
              val continueRunning = alloc[Int](1)
              multi.perform(continueRunning)
              stillRunning = !continueRunning

              if (stillRunning != 0) {
                if (multi.poll(1000) != CURLM_OK) continue = false
              }
            }
            clean()

            }
          })
        
          t.start()
        }


        val streamHeaders =  StreamHeaders(
            url,
            responseCode,
            StatusMessages.byStatusCode.getOrElse(responseCode, ""),
            headerData.data,
            redirectedFrom
          )

        val deGzip = autoDecompress && headerData.data.get("content-encoding").toSeq.flatten.exists(_.contains("gzip"))
        val deDeflate = autoDecompress && headerData.data.get("content-encoding").toSeq.flatten.exists(_.contains("deflate"))
        def persistCookies() = {
                if (sess.persistCookies) {
                  headerData.data
                    .get("set-cookie")
                    .iterator
                    .flatten
                    .flatMap(Parser.cookies(_))
                    .foreach(c => sess.cookies(c.getName()) = c)
                }
              }
        onHeadersReceived(streamHeaders)
        persistCookies()
          
        def processWrappedStream[V](f: java.io.InputStream => V): V = {
          // The HEAD method is identical to GET except that the server
          // MUST NOT return a message-body in the response.
          // https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html section 9.4
          if (upperCaseVerb == "HEAD") f(new ByteArrayInputStream(Array()))
          else {
            f(
              if (deGzip) new GZIPInputStream(input)
              else if (deDeflate) new InflaterInputStream(input)
              else input
            )
          }
        }

        if (streamHeaders.statusCode == 304 || streamHeaders.is2xx || !check) processWrappedStream(f)
        else {
          val errorOutput = new ByteArrayOutputStream()
          processWrappedStream(geny.Internal.transfer(_, errorOutput))
          throw new RequestFailedException(
            Response(
              streamHeaders.url,
              streamHeaders.statusCode,
              streamHeaders.statusMessage,
              new geny.Bytes(errorOutput.toByteArray),
              streamHeaders.headers,
              streamHeaders.history
            )
          )
        }
      }
    }
  }

  private def usingOutputStream[T](os: OutputStream)(fn: OutputStream => T): Unit = 
    try fn(os) finally os.close()

  private def checkCURL(code: CurlECode, msg: String): Unit =
    if (code != CURLE_OK) throw new Exception(s"CURL Error: $code, $msg.")

  private def checkCURLU(code: CurlUCode): Unit =
    if (code != CURLUE_OK) throw new Exception(s"CURL URL error: $code.")

  private def checkCURLM(code: CurlMCode, msg: String): Unit =
    if (code != CURLM_OK) throw new Exception(s"CURL Multi Error: $code, $msg.")

  private def appendHeader(slist: Ptr[CurlSlist], key: String, value: String)(implicit zone: Zone): Ptr[CurlSlist] = {
    val res = slist.append(s"$key:$value")
    if (res == null) {
      throw new Exception(s"Failed to append header $key:$value")
    }
    res
  }


}

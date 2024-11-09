package requests

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.{Files, Path}

import utest._
import java.nio.file.StandardCopyOption
import java.io.FileInputStream

object ModelTests extends TestSuite{

  val tests = Tests {
    test("multipart file uploads should contain application/octet-stream content type") {
      val file = File.createTempFile("multipart_test2", null)
      file.deleteOnExit()
      val license = FileUtils.getFile("license.zip")
      if (license == null) {
        throw new Exception("license.zip not found")
      }
      Files.copy(license, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
      val path = file.toPath()
      val fileKey = "fileKey"
      val fileName = "fileName"
      
      val javaFileMultipart = MultiPart(
        MultiItem(
          fileKey,
          file,
          fileName
        )
      )

      val nioPathMultipart = MultiPart(
        MultiItem(
          fileKey,
          path,
          fileName
        )
      )
      
      val javaFileOutputStream = new ByteArrayOutputStream()
      val nioPathOutputStream = new ByteArrayOutputStream()
      
      javaFileMultipart.write(javaFileOutputStream)
      nioPathMultipart.write(nioPathOutputStream)
      
      val javaFileString = new String(javaFileOutputStream.toByteArray)
      val nioPathString = new String(nioPathOutputStream.toByteArray)
      
      assert(javaFileString.contains("Content-Type: application/octet-stream"))
      assert(nioPathString.contains("Content-Type: application/octet-stream"))
    }
  }
}

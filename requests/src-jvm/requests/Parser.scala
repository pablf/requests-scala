package requests 

import scala.collection.JavaConverters._

object Parser {
    def cookies(header: String) = java.net.HttpCookie.parse(header).asScala
}
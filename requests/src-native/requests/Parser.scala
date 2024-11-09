package requests 

object Parser {

    def cookies(header: String) = {
        val list = java.net.HttpCookie.parse(header)
        val arr = new Array[java.net.HttpCookie](list.size)
        list.toArray(arr)
        arr.toList
    }
}
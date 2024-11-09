#if defined(REQUESTS_SCALA_AUX)
#include <curl/curl.h> 
int requests_scala_curl_setopt_int(CURL *curl, CURLoption opt, int arg) {return curl_easy_setopt(curl, opt, arg); }
int requests_scala_curl_setopt_long(CURL *curl, CURLoption opt, long arg) {return curl_easy_setopt(curl, opt, arg); }
int requests_scala_curl_setopt_pointer(CURL *curl, CURLoption opt, void* arg) {return curl_easy_setopt(curl, opt, arg); }
int requests_scala_curl_getinfo_pointer(CURL *curl, CURLINFO info, void* arg) {return curl_easy_getinfo(curl, info, arg); }
#endif

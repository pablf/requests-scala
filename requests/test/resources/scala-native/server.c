#if defined(REQUESTS_SCALA_TEST_SERVER)
#include <microhttpd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <zlib.h>

#define PORT 8888

int decompress_gzip(const char *input, size_t input_len, char *output, size_t *output_len) {
    z_stream stream;
    memset(&stream, 0, sizeof(stream));

    if (inflateInit2(&stream, 15 | 16) != Z_OK) {
        return -1;
    }

    stream.next_in = (Bytef *)input;
    stream.avail_in = input_len;
    stream.next_out = (Bytef *)output;
    stream.avail_out = *output_len;

    int ret = inflate(&stream, Z_FINISH);
    if (ret != Z_STREAM_END) {
        inflateEnd(&stream);
        return -1;
    }

    *output_len = stream.total_out;
    inflateEnd(&stream);
    return 0;
}

int decompress_deflate(const char *input, size_t input_len, char *output, size_t *output_len) {
    z_stream stream;
    memset(&stream, 0, sizeof(stream));

    if (inflateInit(&stream) != Z_OK) {
        return -1;
    }

    stream.next_in = (Bytef *)input;
    stream.avail_in = input_len;
    stream.next_out = (Bytef *)output;
    stream.avail_out = *output_len;

    int ret = inflate(&stream, Z_FINISH);
    if (ret != Z_STREAM_END) {
        inflateEnd(&stream);
        return -1;
    }

    *output_len = stream.total_out;
    inflateEnd(&stream);
    return 0;
}

static enum MHD_Result answer_to_connection(void *cls, struct MHD_Connection *connection,
                         const char *url, const char *method, const char *version,
                         const char *upload_data, size_t *upload_data_size, void **ptr) {

    if (*upload_data_size == 0) {
      if (*ptr == NULL) {
        return MHD_YES;
      } else {
        struct MHD_Response *response;
        int ret;
        char *r = *ptr;

        r++;
        r[strlen(r)-1] = 0;
        r[strlen(r)-1] = 0;

        response = MHD_create_response_from_buffer(strlen(r), (void *)r, MHD_RESPMEM_PERSISTENT);
        ret = MHD_queue_response(connection, MHD_HTTP_OK, response);
        MHD_destroy_response(response);

        ptr = NULL;

        return ret;
      }
      
    } else {
      const char *contentEncodingHeader = MHD_lookup_connection_value(
        connection,
        MHD_HEADER_KIND,
        "Content-Encoding"
        );
      
      // Determine the compression type based on the header
      int compressionType;
      if (contentEncodingHeader == NULL) {
        compressionType = 0; // None
      } else {
        if (strstr(contentEncodingHeader, "gzip") != NULL) {
          compressionType = 1;
        } else if (strstr(contentEncodingHeader, "deflate") != NULL) {
          compressionType = 2;
        } else {
          compressionType = 0;
        }
      }

      if (compressionType == 0) {
        char *result = malloc(strlen(upload_data) + 2);

        strcpy(result, ".");
        strcat(result, upload_data);

        *ptr = result;
      } else if (compressionType == 1) {
        // Gzip
        char *output = malloc(1024);
        size_t output_len = 1024;

        decompress_gzip(upload_data, *upload_data_size, output, &output_len);
        char *result = malloc(strlen(output) + 2);

        strcpy(result, ".");
        strcat(result, output);
        result[strlen(result)-1] = 0;

        *ptr = result;
        free(output);
      } else if (compressionType == 2) {
        // Deflate
        char *output = malloc(1024);
        size_t output_len = 1024;
        
        decompress_deflate(upload_data, *upload_data_size, output, &output_len);
        char *result = malloc(strlen(output) + 2);

        strcpy(result, ".");
        strcat(result, output);
        result[strlen(result)-1] = 0;

        *ptr = result;
        free(output);
      }

      *upload_data_size = 0;
      return MHD_YES;
    }
    
}

struct MHD_Daemon* start() {
    struct MHD_Daemon *daemon;

    daemon = MHD_start_daemon(MHD_USE_SELECT_INTERNALLY | MHD_USE_DEBUG, PORT, NULL, NULL,
                              &answer_to_connection, NULL, MHD_OPTION_CONNECTION_TIMEOUT, (unsigned int) 120, MHD_OPTION_END);
    if (NULL == daemon) return NULL;

    printf("Server is running on port %d\n", PORT);
    
    return daemon;
}

int port() {
    return PORT;
}

void stop(struct MHD_Daemon *daemon) {
  MHD_stop_daemon(daemon);
}
#endif
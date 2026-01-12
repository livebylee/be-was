package webserver;

import http.HttpResponse;
import http.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class StaticResourceProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StaticResourceProcessor.class);

    public void process(String path, HttpResponse response) {
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex == -1 && !path.endsWith("/")) {
            response.response302Header(path + "/");
            return;
        }

        if (path.endsWith("/")) {
            path += "/index.html";
        }

        String extension = "html";
        dotIndex = path.lastIndexOf(".");
        if (dotIndex != -1) {
            extension = path.substring(dotIndex + 1);
        }
        String contentType = MimeType.getContentType(extension);

        String resourcePath = "/static" + path;
        try (InputStream resourceStream = getClass().getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                response.response404Header();
                return;
            }
            byte[] body = util.IOUtils.readAllBytes(resourceStream);
            response.response200Header(body.length, contentType);
            response.responseBody(body);

        } catch (IOException e) {
            logger.error("file read error");
        }
    }

}

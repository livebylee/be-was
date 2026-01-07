package webserver;

import http.HttpResponse;
import http.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class StaticResourceProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StaticResourceProcessor.class);

    public boolean isExist(String path) {
        String resourcePath = "/static" + (path.endsWith("/") ? path + "index.html" : path);
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }

    public void process(String path, HttpResponse response) {
        if (path.lastIndexOf(".") == -1 && !path.endsWith("/")) {
            response.response302Header(path + "/");
            return;
        }
        String normalizedPath = path.endsWith("/") ? path + "index.html" : path;
        String extension = extractExtension(normalizedPath);
        String contentType = MimeType.getContentType(extension);

        String resourcePath = "/static" + normalizedPath;

        try (InputStream resourceStream = getClass().getResourceAsStream(resourcePath)) {
            byte[] body = util.IOUtils.readAllBytes(resourceStream);
            response.response200Header(body.length, contentType);
            response.responseBody(body);
        } catch (IOException e) {
            logger.error("file read error");
        }
    }

    private String extractExtension(String path) {
        int dotIndex = path.lastIndexOf(".");
        return (dotIndex != -1) ? path.substring(dotIndex + 1) : "html";
    }
}

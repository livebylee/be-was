package webserver;

import http.ContentType;
import http.HttpResponse;
import http.HttpStatus;
import http.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class StaticResourceProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StaticResourceProcessor.class);

    public boolean isExistPath(String path) {
        String resourcePath = "/static" + (path.endsWith("/") ? path + "index.html" : path);
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }

    public void process(String path, HttpResponse response) {
        if (path.lastIndexOf(".") == -1 && !path.endsWith("/")) {
            response.sendRedirect(path + "/");
            return;
        }
        String normalizedPath = path.endsWith("/") ? path + "index.html" : path;
        String extension = extractExtension(normalizedPath);
        ContentType contentType = ContentType.from(extension);

        String resourcePath = "/static" + normalizedPath;

        try (InputStream resourceStream = getClass().getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                response.sendError(HttpStatus.NOT_FOUND, "Resource not found: " + path);
                return;
            }
            byte[] body = util.IOUtils.readAllBytes(resourceStream);
            response.forward(body, contentType);
        } catch (IOException e) {
            logger.error("file read error");
        }
    }

    private String extractExtension(String path) {
        int dotIndex = path.lastIndexOf(".");
        return (dotIndex != -1) ? path.substring(dotIndex + 1) : "html";
    }
}

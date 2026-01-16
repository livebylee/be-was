package webserver;

import http.ContentType;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import http.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;

public class StaticResourceProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StaticResourceProcessor.class);
    private static final String UPLOAD_PATH_PREFIX = "/img_uploads/";
    private static final String UPLOAD_DIRECTORY = "img_uploads/";

    public boolean isExistPath(String path) {

        if (path.startsWith(UPLOAD_PATH_PREFIX)) {
            String fileName = decodePath(path.substring(UPLOAD_PATH_PREFIX.length()));
            File file = new File(UPLOAD_DIRECTORY + fileName);
            return file.exists();
        }

        String resourcePath = "/static" + (path.endsWith("/") ? path + "index.html" : path);
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }

    public void process(HttpRequest request, HttpResponse response) {
        String path = request.getPath();
        String queryString = request.getQueryString();

        if (path.startsWith(UPLOAD_PATH_PREFIX)) {
            serveFromDisk(path, response);
            return;
        }
        if (path.lastIndexOf(".") == -1 && !path.endsWith("/")) {
            String redirectPath = path + "/";
            if (queryString != null) {
                redirectPath += "?" + queryString;
            }
            response.sendRedirect(redirectPath);
            return;
        }
        String normalizedPath = path.endsWith("/") ? path + "index.html" : path;
        String extension = extractExtension(normalizedPath);
        String contentTypeValue = MimeType.getContentType(extension);

        String resourcePath = "/static" + normalizedPath;

        try (InputStream resourceStream = getClass().getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                response.sendError(HttpStatus.NOT_FOUND, "Resource not found: " + path);
                return;
            }
            byte[] body = util.IOUtils.readAllBytes(resourceStream);
            response.forwardWithContentType(body, contentTypeValue);
        } catch (IOException e) {
            logger.error("file read error");
        }
    }

    private void serveFromDisk(String path, HttpResponse response) {
        try {
            String fileName = decodePath(path.substring(UPLOAD_PATH_PREFIX.length()));
            File file = new File(UPLOAD_DIRECTORY + fileName);

            logger.debug("실제 파일 찾기 시도 (절대경로): {}", file.getAbsolutePath());
            if (!file.exists()) {
                response.sendError(HttpStatus.NOT_FOUND, "Image not found on disk");
                return;
            }

            byte[] body = Files.readAllBytes(file.toPath());
            String extension = extractExtension(path);
            response.forwardWithContentType(body, MimeType.getContentType(extension));
            logger.debug("Disk Resource Served: {}", file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Disk file read error: {}", e.getMessage());
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR, "File read error");
        }
    }

    private String decodePath(String path) {
        String decoded = URLDecoder.decode(path, StandardCharsets.UTF_8);
        return Normalizer.normalize(decoded, Normalizer.Form.NFC);
    }

    private String extractExtension(String path) {
        int dotIndex = path.lastIndexOf(".");
        return (dotIndex != -1) ? path.substring(dotIndex + 1) : "html";
    }
}

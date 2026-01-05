package webserver;

import java.io.*;
import java.net.Socket;

import http.MimeType;
import http.HttpRequest;
import http.HttpResponse;

import util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            // inputstream reader
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            String path = request.getPath();

            if (path.startsWith("/user/create")) {
                createUser(response);
            } else {
                responseStaticFile(path, response);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void createUser(HttpResponse response) {
        response.response302Header("/index.html");

    }

    private void responseStaticFile(String path, HttpResponse response) {
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
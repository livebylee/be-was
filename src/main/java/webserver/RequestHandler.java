package webserver;

import java.io.*;
import java.net.Socket;

import http.HttpRequest;
import http.HttpResponse;

import model.UserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private static final StaticResourceProcessor processor = new StaticResourceProcessor();
    private final UserHandler userHandler = new UserHandler();

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
                userHandler.createUser(request, response);
            } else {
                processor.process(path, response);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
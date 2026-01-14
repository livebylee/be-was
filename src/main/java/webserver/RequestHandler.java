package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Set;

import db.Database;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

import http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.controller.Controller;
import webserver.controller.RequestMapping;


public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private final StaticResourceProcessor processor;

    public RequestHandler(Socket connectionSocket, StaticResourceProcessor staticResourceProcessor) {
        this.connection = connectionSocket;
        this.processor = staticResourceProcessor;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            // inputstream reader
            HttpResponse response = new HttpResponse(out);

            try {
                HttpRequest request = new HttpRequest(in);
                String path = request.getPath();

                if (AuthChecker.checkAuthentication(request, response)) {
                    return;
                }

                HttpMethod method = request.getMethod();
                Controller controller = RequestMapping.getController(method, path);

                if (controller != null) {
                    controller.process(request, response);
                } else if (RequestMapping.isExistUrl(path)) {  //url 있는데 메소드 틀림
                    //response.response405; //추후 구현
                } else if (processor.isExistPath(path)) {
                    processor.process(request, response);
                } else {
                    response.sendError(HttpStatus.NOT_FOUND, " 404 error");
                }
            } catch (IllegalArgumentException e) {
                logger.error("Bad Request: {}", e.getMessage());
                response.sendError(HttpStatus.BAD_REQUEST, "400 error");
            } catch (Exception e) {
                logger.error("Internal Server Error: ", e);
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR, "500 error");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
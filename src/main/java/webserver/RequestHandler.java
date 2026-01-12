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
    private static final StaticResourceProcessor processor = new StaticResourceProcessor();

    private static final Set<String> protectedPaths = Set.of("/mypage");

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
            HttpResponse response = new HttpResponse(out);

            try {
                HttpRequest request = new HttpRequest(in);
                String path = request.getPath();

                if (isProtectedPath(path)) {
                    if (!isLoggedIn(request)) {
                        logger.debug("로그인하지 않은 사용자의 허용되지 않은 경로 접근 :{}", path);
                        response.sendRedirect("/login");
                        return;
                    }
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

    private boolean isProtectedPath(String path) {
        return protectedPaths.contains(path);
    }

    private boolean isLoggedIn(HttpRequest request) {
        String sid = request.getCookie("sid");
        return sid != null && Database.getUserBySessionId(sid) != null;
    }
}
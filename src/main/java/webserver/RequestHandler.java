package webserver;

import java.io.*;
import java.net.Socket;

import http.HttpRequest;
import http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.controller.Controller;
import webserver.controller.RequestMapping;


public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private static final StaticResourceProcessor processor = new StaticResourceProcessor();

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
            Controller controller = RequestMapping.getController(path);

            if (controller != null) {
                controller.process(request, response);
            } else {  // 컨트롤러 없을 때정적 파일 처리
                processor.process(path, response);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
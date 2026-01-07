package webserver;

import java.io.*;
import java.net.Socket;

import http.HttpMethod;
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
            try {
                HttpRequest request = new HttpRequest(in);
                HttpResponse response = new HttpResponse(out);

                String path = request.getPath();
                HttpMethod method = request.getMethod();
                Controller controller = RequestMapping.getController(method, path);

                if (controller != null) {
                    controller.process(request, response);
                } else if (RequestMapping.existUrl(path)) {  //url 있는데 메소드 다름
                    //response.response405; //추후 구현
                } else {
                    if(processor.existFile(path)){
                        processor.process(path);
                    }else{
                        //response.response404
                    }
                }
            } catch (IllegalArgumentException e) {
                logger.error("Bad Request: {}", e.getMessage());
                HttpResponse response = new HttpResponse(out);
                //response.response404header  //추후 구현
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
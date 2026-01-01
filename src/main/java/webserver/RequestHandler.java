package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    private static final Map<String, String> mimeTypes = Map.of(
            "html", "text/html",
            "css", "text/css",
            "js", "application/javascript",
            "ico", "image/x-icon",
            "png", "image/png",
            "jpg", "image/jpeg",
            "svg","image/svg+xml"
    );

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            // inputstream reader
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String line = br.readLine();

            String[] tokens = line.split(" ");

            String request_URL = tokens[1];     //  extract path

            int index = request_URL.lastIndexOf(".");   //  extract extension
            String extension = "";
            if(index >0){
                extension = request_URL.substring(index+1);
            }
            String contentType = findType(extension);
            //logger.debug("extension: {}",extension);   // check extension

            logger.debug("request line: {}", line);

            while((line = br.readLine()) != null && !line.equals("")){
                logger.debug("Header: {}", line);
            }

            DataOutputStream dos = new DataOutputStream(out);


            //check file exist
            File file = new File("./src/main/resources/static"+request_URL);

            if(file.exists()){
                byte[] body =
            }
            byte[] body = Files.readAllBytes(new File("./src/main/resources/static/" + request_URL).toPath());
            response200Header(dos, body.length, contentType);
            responseBody(dos, body);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private String findType(String extension){
        return mimeTypes.get(extension);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            //dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Type: " + contentType +"\r\n");

            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response404Header(DataOutputStream dos){
        try {
            dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
            dos.writeBytes("Content-Type: text/html;\r\n");
            dos.writeBytes("\r\n");
            dos.writeBytes("<h1>404 not found</h1>");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}

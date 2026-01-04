package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
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
            "svg", "image/svg+xml"
    );

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            // inputstream reader
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String line = br.readLine();
            if (line == null) return;
            String[] tokens = line.split(" ");
            String method = tokens[0];      //하드코딩 문제 tokens[1]이 없을 수도 있음
            String request_URL = tokens[1];     //  extract path

            String path = request_URL;
            String queryString = "";
            int qindex = request_URL.lastIndexOf("?");

            if (qindex != -1) {  // data exist
                path = request_URL.substring(0, qindex);
                queryString = request_URL.substring(qindex + 1);
            }

            while ((line = br.readLine()) != null && !line.equals("")) {
                logger.debug("Header: {}", line);
            }

            if (path.startsWith("/user/create")) {
                createUser(queryString, dos);
            } else {
                responseStaticFile(path, dos);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private String findType(String extension) {
        return mimeTypes.get(extension);
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024]; // 1kb

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private void createUser(String queryString, DataOutputStream dos) {
        Map<String, String> params = parseQueryString(queryString);

        //user 데이터 파싱 , 저장 로직
        response302Header(dos, "/index.html");

    }

    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] tokens = pair.split("=");
            if (tokens.length == 2) {
                String key = tokens[0];
                String value = URLDecoder.decode(tokens[1], StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;

    }

    private void responseStaticFile(String path, DataOutputStream dos) {
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex == -1 && !path.endsWith("/")) {
            response302Header(dos, path + "/");
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
        String contentType = findType(extension);

        String resourcePath = "/static" + path;
        try (InputStream resourceStream = getClass().getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                response404Header(dos);
                return;
            }
            byte[] body = readAllBytes(resourceStream);

            response200Header(dos, body.length, contentType);
            responseBody(dos, body);

        } catch (IOException e) {
            logger.error("file read error");
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            //dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");

            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response404Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
            dos.writeBytes("Content-Type: text/html;\r\n");
            dos.writeBytes("\r\n");
            dos.writeBytes("<h1>404 not found</h1>");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + " \r\n");
            dos.writeBytes("\r\n");
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
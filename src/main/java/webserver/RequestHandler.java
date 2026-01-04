package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import http.HttpResponse;

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
            HttpResponse response = new HttpResponse(out);
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
                createUser(queryString, response);
            } else {
                responseStaticFile(path, response);
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

    private void createUser(String queryString, HttpResponse response) {
        Map<String, String> params = parseQueryString(queryString);

        //user 데이터 파싱 , 저장 로직
        response.response302Header("/index.html");

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
        String contentType = findType(extension);

        String resourcePath = "/static" + path;
        try (InputStream resourceStream = getClass().getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                response.response404Header();
                return;
            }
            byte[] body = readAllBytes(resourceStream);

            response.response200Header(body.length, contentType);
            response.responseBody(body);

        } catch (IOException e) {
            logger.error("file read error");
        }
    }

}
package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtils;
import webserver.RequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static http.HttpMethod.from;


public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private HttpMethod method;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();


    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String line = br.readLine();
            if (line == null) return;

            parseRequestLine(line);
            parseHeaders(br);

            if (headers.containsKey("Content-Length") && headers.get("Content-Length") != null) {
                parseBody(br);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 여기 하드코딩 바꾸기
    private void parseRequestLine(String requestLine) {
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IllegalArgumentException("Empty reqeust line");
        }
        String[] tokens = requestLine.split(" ");
        if (tokens.length < 2) {//method, path도 없는 경우
            throw new IllegalArgumentException("Invalid Http Request Line");
        }
        this.method = from(tokens[0]);
        parseUrl(tokens[1]);
    }

    private void parseUrl(String url) {
        int qindex = url.lastIndexOf("?");
        if (qindex == -1) {
            this.path = url;
            return;
        }
        this.path = url.substring(0, qindex);
        parseQueryString(url.substring(qindex + 1));
    }

    private void parseQueryString(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return;
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
    }

    private void parseHeaders(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            String[] headerTokens = line.split(":");
            if (headerTokens.length >= 2) {
                String key = headerTokens[0].trim();
                String value = line.substring(line.indexOf(":") + 1).trim();
                headers.put(key, value);
            }
            HttpRequest.logger.debug("Header: {}", line);
        }
    }

    private void parseBody(BufferedReader br) throws IOException {
        String contentType = headers.get("Content-Type");
        int contentLength = Integer.parseInt(headers.get("Content-Length"));

        byte[] bodyBytes = readBodyBytes(in, contentLength);  ///바디 읽기
        if (contentType.contains("")) {
            // 타입별 파싱 함수
        }
    }

    public String getPath() {
        return this.path;
    }

    public Map<String, String> getParams() {
        return this.params;
    }

    public HttpMethod getMethod() {
        return this.method;
    }
}

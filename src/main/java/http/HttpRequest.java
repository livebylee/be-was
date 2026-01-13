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
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private HttpMethod method;
    private String path;
    private String queryString;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();


    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String line = br.readLine();
            if (line == null) return;

            parseRequestLine(line);
            logger.debug("Method:{}, path :{} ", method, path);
            parseHeaders(br);

            if (headers.containsKey("content-length") && headers.get("content-length") != null) {
                logger.debug("Content-Length: {}, Content-Type: {}", headers.get("content-length"), headers.get("content-type"));
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
            this.queryString = null;
            return;
        }
        this.path = url.substring(0, qindex);
        this.queryString = url.substring(qindex + 1);
        parseQueryString(this.queryString);
    }

    private void parseQueryString(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int index = pair.indexOf("=");
            if (index > 0) {
                String key = pair.substring(0, index);
                String value = pair.substring(index + 1);
                params.put(key, URLDecoder.decode(value, StandardCharsets.UTF_8));
            } else if (!pair.isEmpty()) {
                params.put(pair, "");
            }
        }
    }

    private void parseHeaders(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            String[] headerTokens = line.split(":");
            if (headerTokens.length >= 2) {
                String key = headerTokens[0].trim().toLowerCase();
                String value = line.substring(line.indexOf(":") + 1).trim();
                headers.put(key, value);

                // Cookie 헤더 파싱
                if ("Cookie".equalsIgnoreCase(key)) {
                    parseCookies(value);
                }
            }
            HttpRequest.logger.debug("Header: {}", line);
        }
    }

    private void parseCookies(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return;
        }
        // Cookie 헤더 형식: "sid=abc123; name=value; ..."
        String[] cookiePairs = cookieHeader.split(";");
        for (String cookiePair : cookiePairs) {
            String[] tokens = cookiePair.trim().split("=", 2);
            if (tokens.length == 2) {
                String key = tokens[0].trim();
                String value = tokens[1].trim();
                cookies.put(key, value);
            }
        }
        logger.debug("Cookies: {}", cookies);
    }

    private void parseBody(BufferedReader br) throws IOException {
        int contentLength = Integer.parseInt(headers.get("content-length"));

        // 텍스트 데이터 기준 ( byte 방식으로 변환 필요)
        char[] bodyChars = new char[contentLength];
        int readCount = 0;
        while (readCount < contentLength) {
            int result = br.read(bodyChars, readCount, contentLength - readCount);
            if (result == -1) break;
            readCount += result;
        }
        String body = new String(bodyChars, 0, readCount);

        ContentType contentType = ContentType.from(headers.get("content-type"));

        switch (contentType) {
            case FORM_URLENCODED -> {
                parseQueryString(body);
                HttpRequest.logger.debug("Body Params (Form) : {}", params);
            }
            default -> {
                HttpRequest.logger.warn("지원하지 않는 컨텐츠타입 : {}", contentType);
                throw new IllegalArgumentException("Unsupported Content-Type: " + contentType);
            }
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public Map<String, String> getParams() {
        return this.params;
    }

    public String getParams(String key) {
        return this.params.get(key);
    }

    public HttpMethod getMethod() {
        return this.method;
    }

    public Map<String, String> getCookies() {
        return this.cookies;
    }

    public String getCookie(String key) {
        return this.cookies.get(key);
    }
}

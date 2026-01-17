package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static http.HttpMethod.from;
import static util.IOUtils.indexOf;


public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private HttpMethod method;
    private String path;
    private String queryString;
    private String boundary;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();


    public HttpRequest(InputStream in) {
        try {

            String line = readLine(in);
            if (line == null) return;

            parseRequestLine(line);
            logger.debug("Method:{}, path :{} ", method, path);
            parseHeaders(in);

            if (headers.containsKey("content-length") && headers.get("content-length") != null) {
                logger.debug("Content-Length: {}, Content-Type: {}", headers.get("content-length"), headers.get("content-type"));
                parseBody(in);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') continue;
            if (b == '\n') break;
            sb.append((char) b);
        }
        if (b == -1 && sb.length() == 0) return null;
        return sb.toString();
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

    private void parseHeaders(InputStream in) throws IOException {
        String line;
        while (!(line = readLine(in)).isEmpty()) {
            String[] headerTokens = line.split(":");
            if (headerTokens.length >= 2) {
                String key = headerTokens[0].trim().toLowerCase();
                String value = line.substring(line.indexOf(":") + 1).trim();
                headers.put(key, value);

                // Cookie 헤더 파싱
                if ("Cookie".equalsIgnoreCase(key)) {
                    parseCookies(value);
                }
                // multipart : boundary파싱
                if ("content-type".equals(key) && value.contains("multipart/form-data")) {
                    // value 예시: "multipart/form-data; boundary=----WebKitFormBoundary..."
                    String[] parts = value.split("boundary=");
                    if (parts.length > 1) {
                        this.boundary = parts[1]; // HttpRequest 클래스에 필드로 저장해두면 나중에 쓰기 편해요!
                        logger.debug("Boundary found: {}", this.boundary);
                    }
                    logger.debug("[boundary] : " + this.boundary);
                }

            }
            logger.debug("Header: {}", line);
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

    private void parseBody(InputStream in) throws IOException {
        int contentLength = Integer.parseInt(headers.get("content-length"));
        byte[] bodyBytes = in.readNBytes(contentLength);

        ContentType contentType = ContentType.from(headers.get("content-type"));

        if (boundary != null || (contentType != null && contentType == ContentType.MULTIPART)) {
            HttpRequest.logger.debug("멀티파트 데이터 파싱 시작 (바운더리: {})", boundary);
            parseMultipartBody(bodyBytes);
        } else if (contentType != null && contentType == ContentType.FORM_URLENCODED) {
            String body = new String(bodyBytes, StandardCharsets.UTF_8);
            parseQueryString(body);
            HttpRequest.logger.debug("일반 폼 데이터 파싱 완료: {}", params);
        } else {
            HttpRequest.logger.warn("지원하지 않는 컨텐츠타입");
            throw new IllegalArgumentException("Unsupported Content-Type: " + contentType);
        }

    }

    private void parseMultipartBody(byte[] bodyBytes) {
        try {
            byte[] boundaryBytes = ("--" + this.boundary).getBytes(StandardCharsets.UTF_8);
            byte[] doubleCrlf = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);

            int startPos = indexOf(bodyBytes, boundaryBytes, 0);

            while (startPos != -1) {
                int nextBoundaryPos = indexOf(bodyBytes, boundaryBytes, startPos + boundaryBytes.length);
                if (nextBoundaryPos == -1) break;

                // 2. 현재 파트의 전체 데이터 추출 (바운더리 사이의 구간)
                // 파트 시작 위치: startPos + boundaryBytes.length + 2 (\r\n)
                // 파트 끝 위치: nextBoundaryPos - 2 (\r\n)
                int partStart = startPos + boundaryBytes.length + 2;
                int partEnd = nextBoundaryPos - 2;

                // 3. 파트 내에서 헤더와 데이터의 경계(\r\n\r\n) 찾기
                int headerEnd = indexOf(bodyBytes, doubleCrlf, partStart);
                if (headerEnd != -1 && headerEnd < partEnd) {
                    // 파트 헤더 추출
                    String partHeader = new String(bodyBytes, partStart, headerEnd - partStart, StandardCharsets.UTF_8);

                    // 실제 데이터 시작 및 끝 계산
                    int dataStart = headerEnd + doubleCrlf.length;
                    int dataEnd = partEnd;
                    byte[] data = java.util.Arrays.copyOfRange(bodyBytes, dataStart, dataEnd);

                    // 4. 헤더 분석 후 처리
                    processPart(partHeader, data);
                }
                startPos = nextBoundaryPos;
            }
        } catch (Exception e) {
            logger.error("멀티파트 파싱 중 에러 발생: {}", e.getMessage());
        }
    }

    private void processPart(String header, byte[] data) {
        if (header.contains("filename=")) {
            // [파일 파트] - 이미지
            String fileName = extractFileName(header);

            if (fileName == null || fileName.isEmpty() || data.length == 0) {
                logger.debug("파일이 전송되지 않았습니다. (Empty file part)");
                return;
            }
            logger.debug("파일 파트 발견: {}, 크기: {} bytes", fileName, data.length);

            // 임시 저장 테스트 (나중에 분리)
            String uniquenessName = IOUtils.saveFile(fileName, data);
            params.put("imagePath", uniquenessName);
        } else {
            // [텍스트 파트] - content 등
            String name = extractName(header);
            String value = new String(data, StandardCharsets.UTF_8);
            params.put(name, value);
            logger.debug("텍스트 파트 발견: {} = {}", name, value);
        }
    }

    private String extractName(String header) {
        // Content-Disposition: form-data; name="content" 에서 content 추출
        int start = header.indexOf("name=\"") + 6;
        int end = header.indexOf("\"", start);
        return header.substring(start, end);
    }

    private String extractFileName(String header) {
        // Content-Disposition: form-data; name="image"; filename="dog.png" 에서 dog.png 추출
        int start = header.indexOf("filename=\"") + 10;
        int end = header.indexOf("\"", start);
        return header.substring(start, end);
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
        int contentLength = Integer.parseInt(headers.get("Content-Length"));

        // 텍스트 데이터 기준 ( byte 방식으로 변환 필요)
        char[] bodyChars = new char[contentLength];
        int readCount = 0;
        while (readCount < contentLength) {
            int result = br.read(bodyChars, readCount, contentLength - readCount);
            if (result == -1) break;
            readCount += result;
        }
        String body = new String(bodyChars, 0, readCount);

        ContentType contentType = ContentType.from(headers.get("Content-Type"));

        switch (contentType) {
            case FORM_URLENCODED:
                parseQueryString(body);
                HttpRequest.logger.debug("Body Params (Form) : {}", params);
                break;

            default:
                HttpRequest.logger.warn("지원하지 않는 컨텐츠타입 : {}", contentType);
                throw new IllegalArgumentException("Unsupported Content-Type: " + contentType);
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

    public String getImagePath() {
        return this.getParams("imagePath");
    }
}

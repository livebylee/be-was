package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);
    private DataOutputStream dos;

    private HttpStatus status = HttpStatus.OK;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setBody(byte[] body) {
        this.body = body;
        addHeader("Content-Length", String.valueOf(body.length));
    }

    public void send() {
        try {
            dos.writeBytes(status.getStatusLine());  //status line

            for (Map.Entry<String, String> entry : headers.entrySet()) {  //header
                dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
            }

            dos.writeBytes("\r\n");

            if (body.length > 0) {
                dos.write(body, 0, body.length);
            }
            dos.flush();
        } catch (IOException e) {
            logger.error("응답 전송 중 오류 :{}", e.getMessage());
        }
    }

    public void forward(byte[] body, ContentType contentType) {
        this.status = HttpStatus.OK;
        this.addHeader("Content-Type", contentType.getValue());
        this.setBody(body);
        this.send();
    }

    public void sendRedirect(HttpStatus status, String url) {
        this.status = status;
        this.headers.clear();
        this.body = new byte[0];
        this.addHeader("Location", url);
        this.send();
    }

    // 302를 기본으로 쓰는 오버로딩 메서드 추가
    public void sendRedirect(String url) {
        sendRedirect(HttpStatus.FOUND, url);
    }

    public void sendError(HttpStatus status, String message) {
        this.status = status;
        this.addHeader("Content-Type", "text/html;charset=utf-8");
        this.setBody(message.getBytes(StandardCharsets.UTF_8));
        this.send();
    }

    public void sendBody(String content, ContentType contentType) {
        byte[] bodyBytes = content.getBytes(StandardCharsets.UTF_8);
        this.status = HttpStatus.OK;
        this.addHeader("Content-Type", contentType.getValue() + ";charset=utf-8");
        this.setBody(bodyBytes);
        this.send();
    }
}

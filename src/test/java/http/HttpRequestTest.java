package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestTest {

    @Test
    @DisplayName("POST 요청 시 바디의 폼 데이터가 정확히 파싱되어야 한다")
    void parsePostWithBody() {
        // 1. 가짜 HTTP 요청 메시지 구성 (헤더 대소문자 섞음)
        String body = "userId=javajigi&password=password&name=JaeSung";
        String rawRequest = "POST /user/create HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Connection: keep-alive\r\n" +
                "content-length: " + body.length() + "\r\n" + // 소문자 테스트
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "\r\n" +
                body;

        // 2. InputStream으로 변환
        InputStream in = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));

        // 3. HttpRequest 생성 (파싱 실행)
        HttpRequest request = new HttpRequest(in);

        // 4. 검증 (Assertion)
        assertThat(request.getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(request.getPath()).isEqualTo("/user/create");
        assertThat(request.getParams("userId")).isEqualTo("javajigi");
        assertThat(request.getParams("name")).isEqualTo("JaeSung");
    }

    @Test
    @DisplayName("Content-Length가 없는 GET 요청도 정상적으로 파싱되어야 한다")
    void parseGetRequest() {
        String rawRequest = "GET /index.html?id=123 HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        InputStream in = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = new HttpRequest(in);

        assertThat(request.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(request.getPath()).isEqualTo("/index.html");
        assertThat(request.getParams("id")).isEqualTo("123");
    }

    @Test
    @DisplayName("쿠키 헤더가 있는 경우 쿠키 맵에 정상 저장되어야 한다")
    void parseCookieHeader() {
        String rawRequest = "GET /logined HTTP/1.1\r\n" +
                "Cookie: logined=true; userId=tester\r\n" +
                "\r\n";

        InputStream in = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = new HttpRequest(in);

        assertThat(request.getCookie("logined")).isEqualTo("true");
        assertThat(request.getCookie("userId")).isEqualTo("tester");
    }
}
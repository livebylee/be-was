package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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

    @Test
    @DisplayName("특수 문자가 포함된 폼 데이터가 정확히 디코딩되어야 한다")
    void parseEncodedBody() {
        String body = "title=" + URLEncoder.encode("안녕+하세요&반가워요", StandardCharsets.UTF_8);
        String rawRequest = "POST /post HTTP/1.1\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "\r\n" +
                body;

        HttpRequest request = new HttpRequest(new ByteArrayInputStream(rawRequest.getBytes()));
        assertThat(request.getParams("title")).isEqualTo("안녕+하세요&반가워요");
    }

    @Test
    @DisplayName("Content-Length가 숫자가 아닌 경우 예외를 던지거나 기본 처리를 해야 한다")
    void invalidContentLength() {
        String rawRequest = "POST / HTTP/1.1\r\n" +
                "Content-Length: NOT_A_NUMBER\r\n" +
                "\r\n";

        // 이 테스트를 통과시키기 위해 parseBody의 Integer.parseInt에 try-catch를 추가하게 될 것입니다.
        assertThatThrownBy(() -> new HttpRequest(new ByteArrayInputStream(rawRequest.getBytes())))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("쿼리 스트링이 물음표로 끝나고 값이 없는 경우를 처리해야 한다")
    void emptyQueryString() {
        String rawRequest = "GET /user/list? HTTP/1.1\r\n\r\n";
        HttpRequest request = new HttpRequest(new ByteArrayInputStream(rawRequest.getBytes()));

        assertThat(request.getPath()).isEqualTo("/user/list");
        assertThat(request.getParams()).isEmpty();
    }

    @Test
    @DisplayName("값이 없거나 구분자가 없는 파라미터도 안전하게 처리해야 한다")
    void parseEdgeCaseParameters() {
        // 1. 시나리오: password는 값이 없고, hobby는 구분자(=)조차 없음
        String body = "userId=javajigi&password=&hobby";
        String rawRequest = "POST /post HTTP/1.1\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "\r\n" +
                body;

        HttpRequest request = new HttpRequest(new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8)));

        // 검증
        assertThat(request.getParams("userId")).isEqualTo("javajigi");

        // 현재 로직에서는 tokens.length == 2 조건 때문에 아래 값들이 null이 나올 가능성이 높습니다.
        // 표준적으로는 빈 문자열("")로 처리하는 것이 안전합니다.
        assertThat(request.getParams("password")).isEqualTo("");
        assertThat(request.getParams("hobby")).isEqualTo("");
    }

    @Test
    @DisplayName("바디에 한글 데이터가 포함된 경우 깨짐 없이 파싱되어야 한다")
    void parseKoreanParameters() {
        // 2. 시나리오: 한글 이름 포함
        String name = "강태웅";
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String body = "name=" + encodedName;

        String rawRequest = "POST /user/create HTTP/1.1\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "\r\n" +
                body;

        HttpRequest request = new HttpRequest(new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8)));

        // 검증: 한글이 깨지지 않고 원본 그대로 복구되는지 확인
        assertThat(request.getParams("name")).isEqualTo(name);
    }
}
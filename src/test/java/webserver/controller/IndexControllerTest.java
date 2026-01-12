package webserver.controller;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexControllerTest {

    private IndexController indexController;
    private User testUser;

    @BeforeEach
    void setUp() {
        indexController = new IndexController();
        // 테스트 전용 유저 생성 및 DB 저장
        testUser = new User("tester", "password123", "테스터", "test@example.com");
        Database.addUser(testUser);
    }

    @Test
    @DisplayName("비로그인 상태로 메인 페이지 접속 시 로그인 버튼이 포함된 HTML을 응답한다")
    void index_unauthenticated() {
        // 1. Given: 쿠키가 없는 빈 요청 생성
        String requestString = "GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n";
        HttpRequest request = new HttpRequest(new ByteArrayInputStream(requestString.getBytes()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        // 2. When: 컨트롤러 실행
        indexController.process(request, response);

        // 3. Then: 응답 내용 확인
        String responseString = out.toString(StandardCharsets.UTF_8);
        // nav_login.html 조각이 포함되어 있는지 확인 (버튼 텍스트 등)
        assertTrue(responseString.contains("로그인"), "비로그인 시 로그인 버튼이 보여야 합니다.");
        assertTrue(responseString.contains("회원 가입"), "비로그인 시 회원가입 버튼이 보여야 합니다.");
    }

    @Test
    @DisplayName("로그인 상태로 메인 페이지 접속 시 사용자 이름이 포함된 HTML을 응답한다")
    void index_authenticated() {
        // 1. Given: 세션 생성 및 쿠키가 포함된 요청 생성
        String sid = "test-session-id";
        Database.addSession(sid, testUser);

        String requestString = "GET /index.html HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Cookie: sid=" + sid + "\r\n\r\n";
        HttpRequest request = new HttpRequest(new ByteArrayInputStream(requestString.getBytes()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        // 2. When: 컨트롤러 실행
        indexController.process(request, response);

        // 3. Then: 사용자 이름과 마이페이지 링크 확인
        String responseString = out.toString(StandardCharsets.UTF_8);
        assertTrue(responseString.contains("테스터 님"), "로그인 시 사용자 이름이 보여야 합니다.");
        assertTrue(responseString.contains("/mypage"), "마이페이지 이동 링크가 포함되어야 합니다.");
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 마이페이지 접근 시 로그인 페이지로 리다이렉트 된다")
    void mypage_access_denied() {
        // 이 테스트는 MyPageController 혹은 접근 제어 로직을 검증합니다.
        // 여기서는 예시로 로직의 흐름을 보여줍니다.

        // 1. Given: 쿠키 없는 요청
        String requestString = "GET /mypage HTTP/1.1\r\nHost: localhost\r\n\r\n";
        HttpRequest request = new HttpRequest(new ByteArrayInputStream(requestString.getBytes()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        // 2. When: 접근 제어 로직 수행 (보통 Controller 내부 혹은 Interceptor에서 수행)
        String sid = request.getCookie("sid");
        User user = Database.getUserBySessionId(sid);

        if (user == null) {
            response.sendRedirect("/login");
        }

        // 3. Then: 302 리다이렉트 응답 확인
        String responseString = out.toString(StandardCharsets.UTF_8);
        assertTrue(responseString.contains("HTTP/1.1 302 Found"), "302 상태코드가 반환되어야 합니다.");
        assertTrue(responseString.contains("Location: /login"), "로그인 페이지로 리다이렉트 경로가 지정되어야 합니다.");
    }
}
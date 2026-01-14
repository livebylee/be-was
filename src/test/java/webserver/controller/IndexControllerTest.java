package webserver.controller;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.AuthChecker;

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
        testUser = new User("tester", "password123", "테스터123", "test@example.com");
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
        String requestString = "GET /mypage/ HTTP/1.1\r\nHost: localhost\r\n\r\n";
        HttpRequest request = new HttpRequest(new ByteArrayInputStream(requestString.getBytes()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        AuthChecker.checkAuthentication(request, response);
        String responseString = out.toString(StandardCharsets.UTF_8);
        assertTrue(responseString.contains("HTTP/1.1 302 Found"), "302 상태코드가 반환되어야 합니다.");
        assertTrue(responseString.contains("Location: /login"), "로그인 페이지로 리다이렉트 경로가 지정되어야 합니다.");
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 글쓰기 페이지 접근 시 로그인 페이지로 리다이렉트 된다")
    void articlepage_access_denied() {
        // 1. Given: 로그인하지 않은 상태의 요청 생성
        String requestString = "GET /article/ HTTP/1.1\r\nHost: localhost\r\n\r\n";
        HttpRequest request = new HttpRequest(new ByteArrayInputStream(requestString.getBytes()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        // 2. When: 실제 우리가 만든 AuthChecker의 로직을 실행!
        // 테스트 코드에서 직접 if문을 쓰지 않고, 검증 대상인 AuthChecker를 호출합니다.
        AuthChecker.checkAuthentication(request, response);

        // 3. Then: 결과 확인
        String responseString = out.toString(StandardCharsets.UTF_8);
        assertTrue(responseString.contains("HTTP/1.1 302 Found"), "로그인이 안 되었으므로 302 응답이 와야 함");
        assertTrue(responseString.contains("Location: /login"), "리다이렉트 경로는 /login이어야 함");
    }
}
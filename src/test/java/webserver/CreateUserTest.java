package webserver;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.junit.jupiter.api.Test;
import webserver.controller.CreateUserController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserTest {

    @Test
    void usercreationsuccess() throws Exception {
        // 1. POST 요청 시뮬레이션 (본문에 데이터 포함)
        String body = "userId=pobi&password=password&name=포비&email=pobi@nextstep.com";
        String requestMessage = "POST /user/create HTTP/1.1\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "\r\n" +
                body;

        InputStream in = new ByteArrayInputStream(requestMessage.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 2. 실행
        HttpRequest request = new HttpRequest(in);
        HttpResponse response = new HttpResponse(out);
        CreateUserController controller = new CreateUserController();
        controller.process(request, response);

        // 3. 검증: DB에 유저가 저장되었는가?
        User savedUser = Database.findUserById("pobi");
        assertNotNull(savedUser);
        assertEquals("포비", savedUser.getName());

        // 4. 검증: 응답이 302 리다이렉트인가?
        String responseHeader = out.toString();
        assertTrue(responseHeader.contains("HTTP/1.1 302 Found"));
        assertTrue(responseHeader.contains("Location: /index.html"));
    }

    @Test
    void getusercreationfail() throws Exception {
        // 1. GET 요청 시뮬레이션
        String requestMessage = "GET /user/create?userId=pobi HTTP/1.1\r\n" +
                "\r\n";

        InputStream in = new ByteArrayInputStream(requestMessage.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpRequest request = new HttpRequest(in);
        HttpResponse response = new HttpResponse(out);
        CreateUserController controller = new CreateUserController();

        // 2. 검증: 예외가 발생하는지 확인
        assertThrows(IllegalArgumentException.class, () -> {
            controller.process(request, response);
        });
    }
}


//using ai
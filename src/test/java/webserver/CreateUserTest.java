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

    @Test
    void userCreationFailWithDuplicateUserId() throws Exception {
        // 1. 첫 번째 사용자 생성
        String body1 = "userId=testuser&password=password1234&name=테스터1&email=test1@test.com";
        String requestMessage1 = "POST /user/create HTTP/1.1\r\n" +
                "Content-Length: " + body1.length() + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "\r\n" +
                body1;

        InputStream in1 = new ByteArrayInputStream(requestMessage1.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();

        HttpRequest request1 = new HttpRequest(in1);
        HttpResponse response1 = new HttpResponse(out1);
        CreateUserController controller1 = new CreateUserController();
        controller1.process(request1, response1);

        // 2. 동일한 아이디로 두 번째 사용자 생성 시도
        String body2 = "userId=testuser&password=password5678&name=테스터2&email=test2@test.com";
        String requestMessage2 = "POST /user/create HTTP/1.1\r\n" +
                "Content-Length: " + body2.length() + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "\r\n" +
                body2;

        InputStream in2 = new ByteArrayInputStream(requestMessage2.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();

        HttpRequest request2 = new HttpRequest(in2);
        HttpResponse response2 = new HttpResponse(out2);
        CreateUserController controller2 = new CreateUserController();

        // 3. 검증: 아이디 중복으로 예외 발생
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            controller2.process(request2, response2);
        });
        assertTrue(exception.getMessage().contains("이미 존재하 는 아이디입니다"));
    }

    @Test
    void userCreationFailWithDuplicateName() throws Exception {
        // 1. 첫 번째 사용자 생성
        String body1 = "userId=user1234&password=password1234&name=hong&email=hong1@test.com";
        String requestMessage1 = "POST /user/create HTTP/1.1\r\n" +
                "Content-Length: " + body1.length() + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "\r\n" +
                body1;

        InputStream in1 = new ByteArrayInputStream(requestMessage1.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();

        HttpRequest request1 = new HttpRequest(in1);
        HttpResponse response1 = new HttpResponse(out1);
        CreateUserController controller1 = new CreateUserController();
        controller1.process(request1, response1);

        // 2. 동일한 이름으로 두 번째 사용자 생성 시도
        String body2 = "userId=user5678&password=password5678&name=hong&email=hong2@test.com";
        String requestMessage2 = "POST /user/create HTTP/1.1\r\n" +
                "Content-Length: " + body2.length() + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "\r\n" +
                body2;

        InputStream in2 = new ByteArrayInputStream(requestMessage2.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();

        HttpRequest request2 = new HttpRequest(in2);
        HttpResponse response2 = new HttpResponse(out2);
        CreateUserController controller2 = new CreateUserController();

        // 3. 검증: 이름 중복으로 예외 발생
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            controller2.process(request2, response2);
        });
        assertTrue(exception.getMessage().contains("이미 존재하는 닉네임입니다"));
    }
}


//using ai
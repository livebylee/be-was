package webserver.controller;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LoginUserController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(CreateUserController.class);

    public void process(HttpRequest request, HttpResponse response) {
        String userId = request.getParams("userId");
        String password = request.getParams("password");

        User user = Database.findUserById(userId);

        if (user != null && user.authenticate(password)) {
            String sessionId = UUID.randomUUID().toString();
            // 세션 저장
            Database.addUser(user);

            response.addHeader("Set-Cookie", "sid=" + sessionId + "; Path=/");
            response.sendRedirect("/index.html");
        } else {
            //로그인 실패 알림 띄우기
        }
    }

}

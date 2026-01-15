package webserver.controller;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CreateUserController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(CreateUserController.class);


    public void process(HttpRequest request, HttpResponse response) {
        Map<String, String> params = request.getParams();
        String userId = request.getParams("userId");
        String name = request.getParams("name");

        if (Database.findUserById(userId) != null) {
            logger.error("이미 존재하는 아이디입니다: {}", userId);
            return;
        }

        // 2. 중복 닉네임 확인
        if (Database.findUserByName(name) != null) {
            logger.error("이미 존재하는 닉네임입니다: {}", name);
            return;
        }

        User user = new User(
                params.get("userId"),
                params.get("password"),
                params.get("name")
        );  //이게 효율적인? 적합한 방법인지 모르겟다..

        logger.debug("New User created : {}", user);
        Database.addUser(user);

        response.sendRedirect("/login/index.html");
    }
}

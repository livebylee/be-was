package model;

import http.HttpRequest;
import http.HttpResponse;

import db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);

    public void createUser(HttpRequest request, HttpResponse response) {

        Map<String, String> params = request.getParams();
        User user = new User(
                params.get("userId"),
                params.get("password"),
                params.get("name"),
                params.get("email")
        );  //이게 효율적인? 적합한 방법인지 모르겟다..

        logger.debug("New User created : {}", user);
        Database.addUser(user);

        response.response302Header("./index.html");
    }
}

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
        try {
            Map<String, String> params = request.getParams();
            User user = new User(
                    params.get("userId"),
                    params.get("password"),
                    params.get("name"),
                    params.get("email")
            );  //이게 효율적인? 적합한 방법인지 모르겟다..

            logger.debug("New User created : {}", user);
            Database.addUser(user);

            response.sendRedirect("/index.html");
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed :{}", e.getMessage());
        }
    }
}

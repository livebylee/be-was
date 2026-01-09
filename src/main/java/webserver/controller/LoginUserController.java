package webserver.controller;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginUserController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(CreateUserController.class);

    public void process(HttpRequest request, HttpResponse response) {
        String userId = request.getParams("userId");
        String password = request.getParams("password");

        User user = Database.findUserById(userId);
    }

}

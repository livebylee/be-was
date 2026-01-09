package webserver.controller;

import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginUserController implements Controller{
    private static final Logger logger = LoggerFactory.getLogger(CreateUserController.class);

    public void process(HttpRequest request, HttpResponse response){
        String userId = request.getParams("userId");
        String password = request.getMethod("password");

    }
}

package webserver.controller;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import webserver.AuthChecker;

public class ArticleController implements Controller {

    public void process(HttpRequest request, HttpResponse response) {

        if (!AuthChecker.isLoggedIn(request)) {  // 이미 로그인 된 사용자만 접근할 수 있는 페이지니까 필요 없나?
            response.sendRedirect("/login");
            return;
        }

        String content = request.getParams("content");
        String userId = Database.getUserBySessionId(request.getCookie("sid")).getUserId();

        // 저장

        // 리다이렉트
    }
}

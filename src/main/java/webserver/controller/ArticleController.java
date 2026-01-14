package webserver.controller;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.AuthChecker;

public class ArticleController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);
    private static final int MAX_CONTENT_LENGTH = 10000;

    public void process(HttpRequest request, HttpResponse response) {

        if (!AuthChecker.isLoggedIn(request)) {
            logger.debug("인증되지 않은 사용자의 접근");
            response.sendRedirect("/login");
            return;
        }

        String content = request.getParams("content");
        User user = Database.getUserBySessionId(request.getCookie("sid"));

        if (content.length() > MAX_CONTENT_LENGTH) {
            logger.warn("content 길이 제한 초과: {} characters (max: {})", content.length(), MAX_CONTENT_LENGTH);
            response.sendRedirect("/article");
            return;
        }
        if (user == null) { //세션 만료 대비
            logger.debug("Session expired or invalid for article creation");
            response.sendRedirect("/login");
            return;
        }

        String userId = user.getUserId();

        Article article = new Article(userId, content);
        Database.addArticle(article);
        logger.debug("New article created by user: {}, content length: {}", userId, content.length());

        response.sendRedirect("/index.html");
    }
}

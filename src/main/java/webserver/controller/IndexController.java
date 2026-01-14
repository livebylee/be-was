package webserver.controller;

import db.Database;
import http.ContentType;
import http.HttpRequest;
import http.HttpResponse;
import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IndexController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);


    public void process(HttpRequest request, HttpResponse response) {
        String html = readFile("/index.html");

        String sid = request.getCookie("sid");
        User user = Database.getUserBySessionId(sid);

        String dynamicHtml = renderArticleSection();

        String authSection = renderAuthSection(user);
        dynamicHtml = dynamicHtml.replace("{{LOGIN_SECTION}}", authSection);

        response.sendBody(dynamicHtml, ContentType.HTML); // 바뀐 내용 전달
    }


    private String readFile(String filePath) {
        String resourcePath = filePath;
        if (resourcePath.startsWith("./")) {    //경로 변환
            resourcePath = resourcePath.substring(1);
        }
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }

        // /static 경로에서 리소스 읽기
        String fullPath = "/static" + resourcePath;
        String content = IOUtils.readResourceAsString(fullPath);

        if (content == null) {
            logger.error("File not found: {}", fullPath);
            throw new RuntimeException("File not found: " + filePath);
        }

        return content;
    }

    private String renderAuthSection(User user) {
        if (user == null) {
            return readFile("/fragments/nav_login.html");
        }
        String logoutHtml = readFile("/fragments/nav_logout.html");
        return logoutHtml.replace("{{userName}}", user.getName());
    }

    private String renderArticleSection() {
        List<Article> articleList = new ArrayList<>(Database.findAllArticles());

        if (articleList.isEmpty()) {
            return readFile("/index.html")
                    .replace("{{ARTICLE_SECTION}}", "게시글이 없습니다")
                    .replace("{{USERID_SECITON", "");
        }

        Article latestArticle = articleList.get(0);  //latest article

        return readFile("/index.html")
                .replace("{{USERID_SECTION}}", latestArticle.getAuthorId())
                .replace("{{ARTICLE_SECTION}}", latestArticle.getContent());
    }
}

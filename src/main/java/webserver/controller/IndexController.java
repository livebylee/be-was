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
        try {
            String html = readFile("/index.html");

            String sid = request.getCookie("sid");
            User user = Database.getUserBySessionId(sid);

            String targetId = request.getParams("id");
            html = renderArticleSection(html, targetId);

            String authSection = renderAuthSection(user);
            html = html.replace("{{LOGIN_SECTION}}", authSection);

            response.sendBody(html, ContentType.HTML); // 바뀐 내용 전달
        } catch (IllegalArgumentException e) {
            logger.warn("article not found: {}", e.getMessage());
            response.sendRedirect("/");
        }
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

    private String renderArticleSection(String html, String targetId) {
        List<Article> articleList = new ArrayList<>(Database.findAllArticles());

        if (articleList.isEmpty()) {
            logger.info("No articles found in database.");
            return html
                    .replace("{{IMAGE_SECTION}}", "이미지가 없습니다")
                    .replace("{{ARTICLE_SECTION}}", "게시글이 없습니다")
                    .replace("{{USERID_SECTION}}", "")
                    .replace("{{PREV_DISABLED}}", "btn-disabled")
                    .replace("{{NEXT_DISABLED}}", "btn-disabled");
        }

        int currentIndex = 0;
        if (targetId != null) {
            boolean found = false;
            for (int i = 0; i < articleList.size(); i++) {
                if (articleList.get(i).getId().equals(targetId)) {
                    currentIndex = i;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Invalid article ID: " + targetId);
            }
        }

        Article nowArticle = articleList.get(currentIndex);

        String prevId = "#";
        String nextId = "#";
        String prevDisabled = "";
        String nextDisabled = "";

        if (currentIndex > 0) {
            prevId = articleList.get(currentIndex - 1).getId();
        } else {
            prevDisabled = "btn-disabled";
        }

        if (currentIndex < articleList.size() - 1) {
            nextId = articleList.get(currentIndex + 1).getId();
        } else {
            nextDisabled = "btn-disabled";
        }

        String imageTag = "";
        if (nowArticle.getImagePath() != null && !nowArticle.getImagePath().isEmpty()) {
            // DB에 저장된 파일명을 사용하여 img 태그를 생성합니다.
            imageTag = "<img class=\"post__img\" src=\"/img_uploads/" + nowArticle.getImagePath() + "\" />";
        }

        return html.replace("{{USERID_SECTION}}", nowArticle.getAuthorId())
                .replace("{{ARTICLE_SECTION}}", nowArticle.getContent())
                .replace("{{IMAGE_SECTION}}", imageTag)
                .replace("{{PREV_ID}}", prevId)
                .replace("{{NEXT_ID}}", nextId)
                .replace("{{PREV_DISABLED}}", prevDisabled)
                .replace("{{NEXT_DISABLED}}", nextDisabled);
    }
}

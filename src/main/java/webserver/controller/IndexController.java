package webserver.controller;

import db.Database;
import http.ContentType;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtils;

import java.util.Map;

public class IndexController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);


    public void process(HttpRequest request, HttpResponse response) {
        String html = readFile("/index.html");

        String sid = request.getCookie("sid");
        User user = Database.getUserBySessionId(sid);

        StringBuilder authHtml = new StringBuilder();

        if (user != null) {
            // 1. 로그인 상태: 사용자 이름(마이페이지 연결) + 로그아웃 버튼
            authHtml.append("<li class=\"header__menu__item\">");
            authHtml.append("  <a class=\"btn btn_ghost btn_size_s\" href=\"/mypage\">")
                    .append(user.getName()).append(" 님</a>");
            authHtml.append("</li>");

            authHtml.append("<li class=\"header__menu__item\">");
            authHtml.append("  <a class=\"btn btn_contained btn_size_s\" href=\"/logout\">로그아웃</a>");
            authHtml.append("</li>");
        } else {
            // 2. 비로그인 상태: 기존 HTML의 로그인/회원가입 버튼 구조 유지
            authHtml.append("<li class=\"header__menu__item\">");
            authHtml.append("  <a class=\"btn btn_contained btn_size_s\" href=\"/login\">로그인</a>");
            authHtml.append("</li>");

            authHtml.append("<li class=\"header__menu__item\">");
            authHtml.append("  <a class=\"btn btn_ghost btn_size_s\" href=\"/registration\">회원 가입</a>");
            authHtml.append("</li>");
        }

        String dynamicHtml = html.replace("{{LOGIN_SECTION}}", authHtml.toString());
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
}

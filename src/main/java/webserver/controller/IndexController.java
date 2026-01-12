package webserver.controller;

import db.Database;
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
            authHtml.append("<li><a href='/mypage'>").append(user.getName()).append("</a></li>");
            authHtml.append("<li><a href='/user/logout' role='button'>로그아웃</a></li>");
            //authHtml.append()
        } else {
            authHtml.append("<li><a href='/user/login.html' role='button'>로그인</a></li>");
            authHtml.append("<li><a href='/user/form.html' role='button'>회원가입</a></li>");
        }
        String dynamicHtml = html.replace("{{LOGIN_SECTION}}", authHtml.toString());
        //response.sendBody(dynamicHtml , "text/html"); // 바뀐 내용 전달
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

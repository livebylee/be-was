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
        String filePath = "./index.html";
        String html = readFile(filePath);

        String sid = request.getCookie("sid");
        User user = Database.getUserBySessionId(sid);
        Map<String, String> params = request.getParams();

        StringBuilder sb = new StringBuilder(html);

        if (user != null) {
            // 로그인 버튼 교체
        }
        // 응답 전송
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

package webserver;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class AuthChecker {

    private static final Logger logger = LoggerFactory.getLogger(AuthChecker.class);
    private static final Set<String> protectedPaths = Set.of("/mypage", "/article");
    private static final Set<String> guestOnlyPaths = Set.of("/login", "/registration");

    // 인증이 필요한 경로인지 확인
    public static boolean isProtectedPath(String path) {
        String normalizedPath = path.endsWith("/") && path.length() > 1
                ? path.substring(0, path.length() - 1)
                : path;
        return protectedPaths.contains(normalizedPath);
    }

    public static boolean isGuestOnlyPath(String path) {
        String normalizedPath = path.endsWith("/") && path.length() > 1
                ? path.substring(0, path.length() - 1)
                : path;
        return guestOnlyPaths.contains(normalizedPath);
    }

    // 로그인 상태 확인
    public static boolean isLoggedIn(HttpRequest request) {
        String sid = request.getCookie("sid");
        return sid != null && Database.getUserBySessionId(sid) != null;
    }

    public static boolean checkAuthentication(HttpRequest request, HttpResponse response) {
        String path = request.getPath();


        if (isProtectedPath(path)) {
            if (!isLoggedIn(request)) {
                logger.debug("로그인하지 않은 사용자의 허용되지 않은 경로 접근 :{}", path);
                response.sendRedirect("/login");
                return true;
            }
        }

        if (isGuestOnlyPath(path)) {
            if (isLoggedIn(request)) {
                logger.debug("로그인한 사용자의 게스트 전용 경로 접근 :{} ", path);
                response.sendRedirect("/index.html");
                return true;
            }
        }
        return false;
    }
}

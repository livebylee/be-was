package webserver.controller;

import http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    private record MethodUrlKey(HttpMethod method, String url) {
    }

    private static final Map<MethodUrlKey, Controller> handlerMap = new HashMap<>();

    static {
        handlerMap.put(new MethodUrlKey(HttpMethod.POST, "/user/create"), new CreateUserController());
        handlerMap.put(new MethodUrlKey(HttpMethod.POST, "/user/login"), new LoginUserController());
    }

    public static Controller getController(HttpMethod method, String url) {
        return handlerMap.get(new MethodUrlKey(method, url));
    }

    public static boolean isExistUrl(String url) {
        return handlerMap.keySet().stream().anyMatch(key -> key.url().equals(url));
    }
}


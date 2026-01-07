package webserver.controller;

import http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    public record MethodUrlKey(HttpMethod method, String url) {
    }

    private static final Map<MethodUrlKey, Controller> handlerMap = new HashMap<>();

    static {
        handlerMap.put(new MethodUrlKey(HttpMethod.POST, "/user/create"), new CreateUserController());
    }

    public static Controller getController(HttpMethod method, String url) {
        return handlerMap.get(new MethodUrlKey(method, url));
    }
}


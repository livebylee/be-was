package webserver.controller;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    private static final Map<String, Controller> handlerMap = new HashMap<>();

    static {
        handlerMap.put("/user/create", new CreateUserController());
    }

    public static Controller getController(String url) {
        return handlerMap.get(url);
    }
}

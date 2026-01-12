package http;

import webserver.controller.Controller;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ContentType {
    JSON("application/json"),
    FORM_URLENCODED("application/x-www-form-urlencoded"),
    MULTIPART("multipart/form-data"),
    HTML("text/html"),
    PLAIN("text/plain"),
    NONE("");   //Enum안에서 예외에 해당하는 값까지 처리해주는게 맞는가??

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    private static final Map<String, ContentType> CONTENT_TYPE_MAP =
            Collections.unmodifiableMap(
                    Stream.of(values()).collect(Collectors.toMap(
                            type -> type.getValue().toLowerCase(), // Key를 소문자로!
                            type -> type
                    ))
            );

    public String getValue() {
        return value;
    }

    public static ContentType from(String contentTypeHeader) {
        return Optional.ofNullable(contentTypeHeader)
                .map(v -> v.split(";")[0].trim().toLowerCase())
                .map(CONTENT_TYPE_MAP::get)
                .orElse(NONE);
    }
}


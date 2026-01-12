package http;

public enum HttpMethod {
    GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS, TRACE, CONNECT;

    public static HttpMethod from(String method) {
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 method type");
        }
    }
}

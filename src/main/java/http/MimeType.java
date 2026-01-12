package http;

import java.util.Arrays;

public enum MimeType {
    HTML("html", "text/html"),
    CSS("css", "text/css"),
    JS("js", "application/javascript"),
    ICO("ico", "image/x-icon"),
    PNG("png", "image/png"),
    JPG("jpg", "image/jpeg"),
    SVG("svg", "image/svg+xml");

    private final String extension;
    private final String contentType;

    MimeType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public static String getContentType(String extension) {
        return Arrays.stream(values())
                .filter(mime -> mime.extension.equals(extension))
                .findFirst() //정확히 왜 findFirst가 필요한지 궁금함
                .orElse(HTML)
                .contentType;
    }

    public String getContentType() {
        return contentType;
    }
}

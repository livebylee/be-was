package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Article {
    private String id;
    private String title;
    private String content;
    private String authorId;
    private LocalDateTime createdAt;

    public Article(String id, String title, String content, String authorId) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.authorId = authorId;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getAuthorId() {
        return authorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Article {
    private String imagePath;
    private String id;
    private String content;
    private String authorId;
    private LocalDateTime createdAt;

    //생성용
    public Article(String authorId, String content, String imagePath) {
        this.id = UUID.randomUUID().toString();
        this.authorId = authorId;
        this.content = content;
        this.imagePath = imagePath;
        this.createdAt = LocalDateTime.now();
    }

    //조회용
    public Article(String id, String authorId, String content, String imagePath, LocalDateTime createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
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

    public String getImagePath() {
        return imagePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
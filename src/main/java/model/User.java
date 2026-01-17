package model;

import db.Database;

public class User {
    private String userId;
    private String password;
    private String name;

    public User(String userId, String password, String name) {
        validate(userId, "아이디를 4자 이상 입력해주세요.");
        validate(password, "비밀번호를 4자 이상 입력해주세요.");
        validate(name, "이름을 4자 이상 입력해주세요.");

        this.userId = userId.trim();
        this.password = password.trim();
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", password=" + password + ", name=" + name + "]";
    }

    private void validate(String value, String errorMessage) {
        if (value == null || value.trim().length() < 4) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public boolean authenticate(String password) {
        return this.password.equals(password != null ? password.trim() : "");
    }

}

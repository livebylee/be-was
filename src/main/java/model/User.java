package model;

public class User {
    private String userId;
    private String password;
    private String name;
    private String email;

    public User(String userId, String password, String name, String email) {
        validate(userId, "아이디를 4자 이상 입력해주세요.");
        validate(password, "비밀번호를 4자 이상 입력해주세요.");
        validate(name, "이름을 4자 이상 입력해주세요.");

        this.userId = userId.trim();
        this.password = password.trim();
        this.name = name;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", password=" + password + ", name=" + name + ", email=" + email + "]";
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
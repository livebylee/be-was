package model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class UserTest {

    @Test
    @DisplayName("유저 생성 시, 입력 정보가 올바르게 저장")
    void userCreationTest() {
        String userId = "123";
        String name = "lee";
        User user = new User(userId, "pw123", name, "test@gmail.com");

        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getName()).contains(name);
        assertThat(user.getEmail()).isNotNull();
    }

    @Test
    @DisplayName("아이디 비어있으면 예외 발생")
    void invalidIdTest() {
        assertThatThrownBy(() -> {
            new User("", "password", "name", "email");
        }).isInstanceOf(IllegalArgumentException.class);
    }
}
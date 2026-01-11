package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        // 테스트마다 공통으로 사용할 유저 객체를 미리 생성합니다.
        user = new User("123", "pw123", "lee", "test@gmail.com");
    }

    @Test
    @DisplayName("유저 생성 시, 입력 정보가 올바르게 저장")
    void userCreationTest() {
        assertThat(user.getUserId()).isEqualTo("123");
        assertThat(user.getName()).contains("lee");
        assertThat(user.getEmail()).isNotNull();
    }

    @Test
    @DisplayName("아이디 비어있으면 예외 발생")
    void invalidIdTest() {
        assertThatThrownBy(() -> {
            new User("", "password", "name", "email");
        }).isInstanceOf(IllegalArgumentException.class);
    }

    // --- 로그인(인증) 관련 테스트 추가 ---

    @Test
    @DisplayName("비밀번호가 일치하면 로그인(인증)에 성공한다")
    void loginSuccessTest() {
        // User 클래스에 authenticate(String password) 메서드가 있다고 가정합니다.
        boolean isSuccess = user.authenticate("pw123");
        assertThat(isSuccess).isTrue();
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인(인증)에 실패한다")
    void loginFailTest() {
        boolean isSuccess = user.authenticate("wrong_pw");
        assertThat(isSuccess).isFalse();
    }

    @Test
    @DisplayName("비밀번호가 null이거나 비어있으면 인증에 실패한다")
    void loginInvalidPasswordTest() {
        assertThat(user.authenticate(null)).isFalse();
        assertThat(user.authenticate("")).isFalse();
    }
}
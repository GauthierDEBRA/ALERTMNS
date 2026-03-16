package com.alertmns.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JwtUtil.class)
@TestPropertySource(properties = {
        "jwt.secret=test_secret_key_for_automated_tests_only_12345678901234567890",
        "jwt.expiration=3600000"
})
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateToken_extractsCorrectEmail() {
        String token = jwtUtil.generateToken("alice@alertmns.fr", 1L, "Admin");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("alice@alertmns.fr");
    }

    @Test
    void generateToken_extractsCorrectUserId() {
        String token = jwtUtil.generateToken("bob@alertmns.fr", 42L, "RH");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void generateToken_extractsCorrectRole() {
        String token = jwtUtil.generateToken("carol@alertmns.fr", 7L, "Collaborateur");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("Collaborateur");
    }

    @Test
    void validateToken_returnsTrueForMatchingEmail() {
        String token = jwtUtil.generateToken("dave@alertmns.fr", 3L, "Admin");
        assertThat(jwtUtil.validateToken(token, "dave@alertmns.fr")).isTrue();
    }

    @Test
    void validateToken_returnsFalseForWrongEmail() {
        String token = jwtUtil.generateToken("eve@alertmns.fr", 5L, "RH");
        assertThat(jwtUtil.validateToken(token, "impostor@alertmns.fr")).isFalse();
    }

    @Test
    void isTokenValid_returnsTrueForFreshToken() {
        String token = jwtUtil.generateToken("frank@alertmns.fr", 9L, "Admin");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForGarbage() {
        assertThat(jwtUtil.isTokenValid("not.a.jwt")).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseForTamperedToken() {
        String token = jwtUtil.generateToken("grace@alertmns.fr", 11L, "Admin");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }
}

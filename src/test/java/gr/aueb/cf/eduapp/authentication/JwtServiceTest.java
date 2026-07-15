package gr.aueb.cf.eduapp.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    // Dummy key for tests only, unrelated to the real app.security.secret-key
    private static final String TEST_SECRET_KEY =
            "Atbb8SCjLsdXPiFT+cY3iAARQNo1F4zL4LqbR5fD7utWoNsquX+EQdGpLndNV7UZ";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3_600_000L);
    }

    @Test
    void generateTokenEncodesSubjectAndRole() {
        String token = jwtService.generateToken("alice", "TEACHER");

        assertEquals("alice", jwtService.extractSubject(token));
        assertEquals("TEACHER", jwtService.getStringClaim(token, "role"));
    }

    @Test
    void isTokenValidReturnsTrueForMatchingUser() {
        String token = jwtService.generateToken("alice", "TEACHER");
        UserDetails userDetails = User.withUsername("alice")
                .password("irrelevant")
                .authorities("ROLE_TEACHER")
                .build();

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValidReturnsFalseForDifferentUser() {
        String token = jwtService.generateToken("alice", "TEACHER");
        UserDetails userDetails = User.withUsername("bob")
                .password("irrelevant")
                .authorities("ROLE_TEACHER")
                .build();

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }
}

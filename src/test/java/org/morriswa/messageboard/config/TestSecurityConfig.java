package org.morriswa.messageboard.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestConfiguration @Slf4j
public class TestSecurityConfig {

    static final String TOKEN = "token";
    static final String SUB = "sms|12345678";

    @Value("${auth0.rbac.permissions}")
    String PERMISSIONS;

    @Value("${testing.email}")
    String TEST_EMAIL;

    @Bean @Profile("test")
    public JwtDecoder jwtDecoder() {
        // This anonymous class needs for the possibility of using SpyBean in test methods
        // Lambda cannot be a spy with spring @SpyBean annotation
        return token -> jwt();
    }

    public Jwt jwt() {
        // This is a place to add general and maybe custom claims which should be available after parsing token in the live system
        var claims = new HashMap<String, Object>(){{
            put("sub", SUB);
            put("permissions", List.of(PERMISSIONS.split(" ")));
            put("email", TEST_EMAIL);
        }};

        //This is an object that represents contents of jwt token after parsing
        return new Jwt(
                TOKEN,
                Instant.now(),
                Instant.now().plusSeconds(30),
                Map.of("alg", "none"),
                claims
        );
    }

}
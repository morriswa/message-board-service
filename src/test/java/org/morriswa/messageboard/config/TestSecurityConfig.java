package org.morriswa.messageboard.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration @Slf4j
public class TestSecurityConfig {

    private final JwtTestUtils utils;

    @Autowired
    public TestSecurityConfig(JwtTestUtils utils) {
        this.utils = utils;
    }

    @Bean @Profile("test")
    public JwtDecoder jwtDecoder() {
        // This anonymous class needs for the possibility of using SpyBean in test methods
        // Lambda cannot be a spy with spring @SpyBean annotation
        return token -> utils.jwt();
    }
}
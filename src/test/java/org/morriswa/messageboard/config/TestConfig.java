package org.morriswa.messageboard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.Properties;

@TestConfiguration
public class TestConfig {

    private final JwtTestUtils utils;

    @Autowired
    public TestConfig(JwtTestUtils utils) {
        this.utils = utils;
    }

    @Bean @Profile("test")
    public JwtDecoder jwtDecoder() {
        // This anonymous class needs for the possibility of using SpyBean in test methods
        // Lambda cannot be a spy with spring @SpyBean annotation
        return token -> utils.jwt();
    }

    @Bean @Profile("test")
    public BuildProperties getTestBuildProps() {
        // This anonymous class needs for the possibility of using SpyBean in test methods
        // Lambda cannot be a spy with spring @SpyBean annotation
        return new BuildProperties(new Properties(){{
            put("name", "message-board-test");
            put("version", "testing-testing-123");
        }});
    }
}
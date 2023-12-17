package org.morriswa.messageboard;

import org.apache.http.HttpHeaders;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.config.JwtTestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class WebSecurityTest extends MessageboardTest {

    @Value("${common.secured-permissions}")
    String PERMISSIONS;

    @MockBean
    private JwtTestUtils config;

    @Test
    void testSuccessful() throws Exception {
        var claims = new HashMap<String, Object>(){{
            put("sub", "sms|12345678");
            put("permissions", List.of(PERMISSIONS.split("\\s")));
            put("email", "test@email.com");
        }};

        when(config.jwt()).thenReturn(new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(30),
                Map.of("alg", "none"),
                claims));

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("common.service.endpoints.health.path"));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(targetUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().is(200))
                .andExpect(jsonPath(
                        "$.message",
                        Matchers.equalTo(e.getRequiredProperty("common.service.endpoints.health.messages.get"))));
    }

    @Test
    void testMissingAllRequiredPermissions() throws Exception {
        var claims = new HashMap<String, Object>(){{
            put("sub", "sms|12345678");
            put("email", "test@email.com");
        }};

        when(config.jwt()).thenReturn(new Jwt(
                    "token",
                    Instant.now(),
                    Instant.now().plusSeconds(30),
                    Map.of("alg", "none"),
                    claims));

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("common.service.endpoints.health.path"));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(targetUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))


                   .andExpect(status().is(403))
                            .andExpect(jsonPath(
                        "$.description",
                        Matchers.equalTo(e.getRequiredProperty("common.service.errors.security.scope-error-message"))));
    }

    @Test
    void testMissingOneRequiredPermissions() throws Exception {
        var claims = new HashMap<String, Object>(){{
            put("sub", "sms|12345678");
            put("permissions", List.of(PERMISSIONS.split(" ")[0]));
            put("email", "test@email.com");
        }};

        when(config.jwt()).thenReturn(new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(30),
                Map.of("alg", "none"),
                claims));

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("common.service.endpoints.health.path"));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(targetUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().is(403))
                .andExpect(jsonPath(
                        "$.description",
                        Matchers.equalTo(e.getRequiredProperty("common.service.errors.security.scope-error-message"))));
    }

    @Test
    void testUnauthorized() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("common.service.endpoints.health.path"));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(targetUrl))
                .andExpect(status().is(401))
                .andExpect(jsonPath(
                        "$.description",
                        Matchers.equalTo(e.getRequiredProperty("common.service.errors.security.not-allowed-desc"))));
    }

    @Test
    void testInvalidJwt() throws Exception {

        when(config.jwt()).thenThrow(OAuth2AuthenticationException.class);

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("common.service.endpoints.health.path"));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(targetUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().is(401))
                .andExpect(jsonPath(
                        "$.error",
                        Matchers.equalTo(e.getRequiredProperty("common.service.errors.security.invalid-jwt"))));
    }
}

package org.morriswa.messageboard;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class HealthEndpointsTest extends MessageboardTest {

    @Test
    void testHealthEndpoint() throws Exception {
        hitPublic("common","health", HttpMethod.GET)
                .andExpect(status().is(200))
                .andExpect(jsonPath(
                        "$.message",
                        Matchers.equalTo(e.getRequiredProperty("common.service.endpoints.health.messages.get"))));
    }

    @Test
    void testSecureHealthEndpoint() throws Exception {

        hit("common","health", HttpMethod.GET)
            .andExpect(status().is(200))
            .andExpect(jsonPath(
                    "$.message",
                    Matchers.equalTo(e.getRequiredProperty("common.service.endpoints.health.messages.get"))));
    }
}

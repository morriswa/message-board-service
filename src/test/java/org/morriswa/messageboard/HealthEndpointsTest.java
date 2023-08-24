package org.morriswa.messageboard;

import org.apache.http.HttpHeaders;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class HealthEndpointsTest extends MessageboardTest {

    @Test
    void testHealthEndpoint() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                    .get("/"+e.getRequiredProperty("common.service.endpoints.health.path")))
                .andExpect(status().is(200))
                .andExpect(jsonPath(
                        "$.message",
                        Matchers.equalTo(e.getRequiredProperty("common.service.endpoints.health.messages.get"))));
    }

    @Test
    void testSecureHealthEndpoint() throws Exception {

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
}

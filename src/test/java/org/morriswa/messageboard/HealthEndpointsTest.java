package org.morriswa.messageboard;

import org.apache.http.HttpHeaders;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.config.TestSecurityConfig;
import org.morriswa.messageboard.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {TestSecurityConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class HealthEndpointsTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private Environment e;

    @MockBean private CommentRepo commentRepo;

    @MockBean private CommunityMemberRepo communityMemberRepo;

    @MockBean private CommunityRepo communityRepo;

    @MockBean private PostRepo postRepo;

    @MockBean private ResourceRepo resourceRepo;

    @MockBean private UserProfileRepo userProfileRepo;

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

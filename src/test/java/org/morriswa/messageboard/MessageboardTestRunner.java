package org.morriswa.messageboard;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.config.TestSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {TestSecurityConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MessageboardTestRunner {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testTestTest() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/v0/health")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
            ).andExpect(status().is(200));
    }
}

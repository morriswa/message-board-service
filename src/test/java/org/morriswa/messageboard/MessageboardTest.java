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
public class MessageboardTest {

    @Autowired protected MockMvc mockMvc;

    @Autowired protected Environment e;

    @MockBean protected CommentRepo commentRepo;

    @MockBean protected CommunityMemberRepo communityMemberRepo;

    @MockBean protected CommunityRepo communityRepo;

    @MockBean protected PostRepo postRepo;

    @MockBean protected ResourceRepo resourceRepo;

    @MockBean protected UserProfileRepo userProfileRepo;

}

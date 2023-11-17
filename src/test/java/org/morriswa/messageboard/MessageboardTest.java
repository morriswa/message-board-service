package org.morriswa.messageboard;

import org.checkerframework.checker.units.qual.A;
import org.morriswa.messageboard.config.JwtTestUtils;
import org.morriswa.messageboard.config.TestSecurityConfig;
import org.morriswa.messageboard.dao.*;
import org.morriswa.messageboard.util.HttpResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(classes = {TestSecurityConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MessageboardTest {

    @Autowired protected MockMvc mockMvc;

    @Autowired protected HttpResponseFactory responseFactory;

    @Autowired protected Environment e;

    @MockBean protected CommentDao commentDao;

    @MockBean protected CommunityMemberDao communityMemberDao;

    @MockBean protected CommunityDao communityRepo;

    @MockBean protected PostDao postDao;

    @MockBean protected PostSessionDao postSessionDao;

    @MockBean protected ResourceDao resourceDao;

    @MockBean protected UserProfileDao userProfileDao;
}

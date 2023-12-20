package org.morriswa.messageboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.morriswa.messageboard.config.TestConfig;
import org.morriswa.messageboard.dao.*;
import org.morriswa.messageboard.util.HttpResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMapAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = {TestConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "testing.email=test@email.com",
        "common.secured-permissions=testone testtwo testthree"
})
@AutoConfigureMockMvc
public class MessageboardTest {

    @Autowired protected MockMvc mockMvc;

    @Autowired protected HttpResponseFactory responseFactory;

    @Autowired protected Environment e;

    @MockBean protected CommentDao commentDao;

    @MockBean protected CommunityMemberDao communityMemberDao;

    @MockBean protected CommunityDao communityRepo;

    @MockBean protected PostDao postDao;

    @MockBean protected PostDraftDao postDraftDao;

    @MockBean protected ResourceDao resourceDao;

    @MockBean protected UserProfileDao userProfileDao;

    protected final String AUTH_ZERO_ID = "abc|123";

    @Value("testing.email")
    protected String TEST_EMAIL;

    protected final String DISPLAY_NAME = "displayName";

    protected final String DEFAULT_TOKEN = "Bearer token";

    final protected ObjectMapper om;

    public MessageboardTest() {
        om = new ObjectMapper();
    }

    public ResultActions hitPublic(
            String service, String endpoint, HttpMethod method) throws Exception {

        final String target = String.format("/%s", e.getRequiredProperty(String.format(
            "%s.service.endpoints.%s.path",
            service,endpoint)));

        return this.mockMvc.perform(MockMvcRequestBuilders.request(method, target));
    }

    public ResultActions hit(
            String service, String endpoint, HttpMethod method) throws Exception {

        final String target = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty(
                        String.format(
                                "%s.service.endpoints.%s.path",
                                service,endpoint)));

        return this.mockMvc.perform(MockMvcRequestBuilders.request(method, target)
                .header("Authorization", DEFAULT_TOKEN));
    }

    public ResultActions hit(
            String service, String endpoint, Map<String, String> params, HttpMethod method) throws Exception {

        final String target = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty(
                        String.format(
                                "%s.service.endpoints.%s.path",
                                service,endpoint)));
        var shutUp = new MultiValueMapAdapter<>(new HashMap<String, List<String>>(){{
            params.forEach((key,val)->put(key,List.of(val)));
        }});

        return this.mockMvc.perform(MockMvcRequestBuilders.request(method, target)
                .header("Authorization", DEFAULT_TOKEN)
                .params(shutUp));
    }

    public ResultActions hit(
            String service, String endpoint, HttpMethod method,
            Object content) throws Exception {

        final String target = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty(
                        String.format(
                                "%s.service.endpoints.%s.path",
                                service,endpoint)));

        return this.mockMvc.perform(MockMvcRequestBuilders.request(method, target)
                .header("Authorization", DEFAULT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(content)));
    }

    public ResultActions hit(
            String service, List<String> endpoint, HttpMethod method,
            Object content) throws Exception {
        assert endpoint.size() >= 2;

        final String target = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty(
                        String.format(
                                "%s.service.endpoints.%s.path",
                                service,endpoint.get(0))));

        return this.mockMvc.perform(MockMvcRequestBuilders.request(method, target, endpoint.subList(1,endpoint.size()).toArray())
                .header("Authorization", DEFAULT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(content)));
    }
}

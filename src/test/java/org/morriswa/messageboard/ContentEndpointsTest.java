package org.morriswa.messageboard;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.control.requestbody.DraftBody;
import org.morriswa.messageboard.enumerated.UserRole;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.User;
import org.morriswa.messageboard.service.CommunityService;
import org.morriswa.messageboard.service.UserProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ContentEndpointsTest extends MessageboardTest {

    @MockBean private UserProfileService userProfileService;

    @MockBean private CommunityService communityService;


    private final String AUTH_ZERO_ID = "abc|123";

    @Value("testing.email")
    private String TEST_EMAIL;

    private final String DISPLAY_NAME = "displayName";

    private final String DEFAULT_TOKEN = "Bearer token";

    private User getExampleUser() {
        return new User(UUID.randomUUID(),
                AUTH_ZERO_ID,
                TEST_EMAIL,
                DISPLAY_NAME,
                UserRole.DEFAULT);
    }

    @Test
    void testCreateEmptyPostDraft() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("content.service.endpoints.create-draft.path"));

        final Long communityId = 1L;

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl, communityId)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.message",
                        Matchers.is(e.getRequiredProperty("content.service.endpoints.create-draft.messages.post"))))
        ;
    }

    @Test
    void testCreatePostDraftOnlyDescription() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("content.service.endpoints.create-draft.path"));

        final Long communityId = 1L;

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final DraftBody body = new DraftBody(null, "Hello!");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl, communityId)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.message",
                        Matchers.is(e.getRequiredProperty("content.service.endpoints.create-draft.messages.post"))))
        ;
    }

    @Test
    void testCreatePostDraftBadCaptionAndFineDescription() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("content.service.endpoints.create-draft.path"));

        final Long communityId = 1L;

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final DraftBody body = new DraftBody("hello", "Hello!");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl, communityId)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error",
                        Matchers.is(ValidationException.class.getSimpleName())))
                .andExpect(jsonPath("$.stack[0].rejectedValue",
                        Matchers.is(body.caption())))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is( String.format(
                                e.getRequiredProperty("content.service.errors.bad-caption-length"),
                                e.getRequiredProperty("content.service.rules.caption.min-length"),
                                e.getRequiredProperty("content.service.rules.caption.max-length")
                        ))))
        ;
    }

    @Test
    void testCreatePostDraftBadCaptionAndLongDescription() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("content.service.endpoints.create-draft.path"));

        final Long communityId = 1L;

        final int MAX_DESC = Integer.parseInt(e.getRequiredProperty("content.service.rules.description.max-length"));

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final String oversizedDescription = "a".repeat(Math.max(0, MAX_DESC + 500));


        final DraftBody body = new DraftBody("hello", oversizedDescription);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl, communityId)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error",
                        Matchers.is(ValidationException.class.getSimpleName())))
                .andExpect(jsonPath("$.stack[0].rejectedValue",
                        Matchers.is(body.caption())))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is( String.format(
                                e.getRequiredProperty("content.service.errors.bad-caption-length"),
                                e.getRequiredProperty("content.service.rules.caption.min-length"),
                                e.getRequiredProperty("content.service.rules.caption.max-length")
                        ))))
                .andExpect(jsonPath("$.stack[1].rejectedValue",
                        Matchers.is(body.description())))
                .andExpect(jsonPath("$.stack[1].message",
                        Matchers.is( String.format(
                                e.getRequiredProperty("content.service.errors.bad-desc-length"),
                                MAX_DESC
                        ))))
        ;
    }
}

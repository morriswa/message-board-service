package org.morriswa.messageboard;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.control.requestbody.DraftBody;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.service.CommunityService;
import org.morriswa.messageboard.service.UserProfileService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ContentEndpointsTest extends MessageboardTest {

    @MockBean private UserProfileService userProfileService;

    @MockBean private CommunityService communityService;

    private final Long DEFAULT_COMMUNITY_ID = 1L;

    @Test
    void testCreateEmptyPostDraft() throws Exception {

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        hit("content", List.of("create-draft", DEFAULT_COMMUNITY_ID.toString()), HttpMethod.POST, new DraftBody(null, null))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.message",
                        Matchers.is(e.getRequiredProperty("content.service.endpoints.create-draft.messages.post"))))
        ;
    }

    @Test
    void testCreatePostDraftOnlyDescription() throws Exception {

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final DraftBody body = new DraftBody(null, "Hello!");

        hit("content", List.of("create-draft", DEFAULT_COMMUNITY_ID.toString()), HttpMethod.POST, body)
        .andExpect(status().is(201))
        .andExpect(jsonPath("$.message",
                Matchers.is(e.getRequiredProperty("content.service.endpoints.create-draft.messages.post"))))
        ;
    }

    @Test
    void testCreatePostDraftBadCaptionAndFineDescription() throws Exception {

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final DraftBody body = new DraftBody("hello", "Hello!");

        hit("content", List.of("create-draft", DEFAULT_COMMUNITY_ID.toString()), HttpMethod.POST, body)
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

        final int MAX_DESC = Integer.parseInt(e.getRequiredProperty("content.service.rules.description.max-length"));

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final String oversizedDescription = "a".repeat(Math.max(0, MAX_DESC + 500));


        final DraftBody body = new DraftBody("hello", oversizedDescription);

        hit("content", List.of("create-draft", DEFAULT_COMMUNITY_ID.toString()), HttpMethod.POST, body)
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

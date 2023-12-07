package org.morriswa.messageboard;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.control.requestbody.CreateCommunityRequestBody;
import org.morriswa.messageboard.control.requestbody.UpdateCommunityRequest;
import org.morriswa.messageboard.enumerated.UserRole;
import org.morriswa.messageboard.exception.NoRegisteredUserException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.model.User;
import org.morriswa.messageboard.service.UserProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommunityEndpointsTest extends MessageboardTest {

    @MockBean private UserProfileService userProfileService;

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
    void testUserShouldBeRegisteredToCreateCommunity() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("community.service.endpoints.community.path"));

        when(userProfileService.authenticate(any()))
                .thenThrow(new NoRegisteredUserException(e.getRequiredProperty("user-profile.service.errors.missing-user")));

        final CreateCommunityRequestBody body
                = new CreateCommunityRequestBody("hello-world","Hello World!");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("user-profile.service.errors.missing-user"))))
                .andExpect(jsonPath("$.error",
                        Matchers.is(NoRegisteredUserException.class.getSimpleName())))
        ;
    }

    @Test
    void testCreateCommunity() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("community.service.endpoints.community.path"));

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final CreateCommunityRequestBody body
                = new CreateCommunityRequestBody("hello-world","Hello World!");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.message",
                        Matchers.is(e.getRequiredProperty("community.service.endpoints.community.messages.post"))))
        ;
    }

    @Test
    void testCreateCommunityBadLocator() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("community.service.endpoints.community.path"));

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final CreateCommunityRequestBody body
                = new CreateCommunityRequestBody("#hello-world","Hello World!");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(e.getRequiredProperty("community.service.errors.bad-community-ref"))))
        ;
    }

    @Test
    void testCreateCommunityShortLocator() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("community.service.endpoints.community.path"));

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final CreateCommunityRequestBody body =
                new CreateCommunityRequestBody("hi","Hello World!");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(
                            String.format(
                                e.getRequiredProperty("community.service.errors.bad-community-ref-length"),
                                e.getRequiredProperty("community.service.rules.community-ref.min-length"),
                                e.getRequiredProperty("community.service.rules.community-ref.max-length"))
                )))
        ;
    }

    @Test
    void testCreateCommunityDuplicateLocator() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("community.service.endpoints.community.path"));

        final String DUPLICATE_NAME = "hello-world";

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        doThrow(new ValidationException(
                "communityRef",
                DUPLICATE_NAME
                ,e.getRequiredProperty("community.service.errors.ref-already-taken")))
            .when(communityRepo)
            .createNewCommunity(any());

        final CreateCommunityRequestBody body = new CreateCommunityRequestBody(DUPLICATE_NAME,"Hello World!");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(e.getRequiredProperty("community.service.errors.ref-already-taken"))))
        ;
    }

    @Test
    void testUpdateNoCommunity() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("community.service.endpoints.community.path"));

        final UUID ownerId = UUID.randomUUID();

        when(userProfileService.authenticate(any())).thenReturn(ownerId);

        when(communityRepo.findCommunity(any(Long.class)))
                .thenReturn(Optional.of(new Community(1L, null, null, ownerId, null, 1)));

        final var request = new UpdateCommunityRequest(
                null,
                "hello-world",
                null,
                null);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(
                                String.format(e.getRequiredProperty("common.service.errors.missing-required-fields"),
                                        "communityId"))))
        ;
    }

    @Test
    void testUpdateCommunityNothing() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("community.service.endpoints.community.path"));

        final UUID ownerId = UUID.randomUUID();

        when(userProfileService.authenticate(any())).thenReturn(ownerId);

        when(communityRepo.findCommunity(any(Long.class))).thenReturn(Optional.of(new Community(1L, null, null, ownerId, null, 1)));

        final var request = new UpdateCommunityRequest(
                1L,
                null,
                null,
                null);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(
                            String.format(e.getRequiredProperty("common.service.errors.missing-required-fields"),
                                    "[communityOwnerUserId OR communityLocator OR communityDisplayName]"))))
        ;
    }

    @Test
    void testUpdateJustLocator() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("community.service.endpoints.community.path"));

        final UUID ownerId = UUID.randomUUID();

        when(userProfileService.authenticate(any())).thenReturn(ownerId);

        when(communityRepo.findCommunity(any(Long.class))).thenReturn(Optional.of(new Community(1L, null, null, ownerId, null, 1)));

        final var request = new UpdateCommunityRequest(
                1L,
                "hello-world2",
                null,
                null);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().is(204))
                .andExpect(jsonPath("$.message",
                        Matchers.is(e.getRequiredProperty("community.service.endpoints.community.messages.patch"))))
        ;
    }

    @Test
    void testUpdateBadLocator() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("community.service.endpoints.community.path"));

        final UUID ownerId = UUID.randomUUID();

        when(userProfileService.authenticate(any())).thenReturn(ownerId);

        when(communityRepo.findCommunity(any(Long.class))).thenReturn(Optional.of(new Community(1L, null, null, ownerId, null, 1)));

        final var request = new UpdateCommunityRequest(
                1L,
                "-hello-world2",
                null,
                null);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(e.getRequiredProperty("community.service.errors.bad-community-ref"))))
        ;
    }

    @Test
    void testUpdateLongLocator() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("community.service.endpoints.community.path"));

        final UUID ownerId = UUID.randomUUID();

        when(userProfileService.authenticate(any())).thenReturn(ownerId);

        when(communityRepo.findCommunity(any(Long.class))).thenReturn(Optional.of(new Community(1L, null, null, ownerId, null, 1)));

        final var request = new UpdateCommunityRequest(
                1L,
                "hello-01234567891123456789hello",
                null,
                null);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(
                            String.format(
                                e.getRequiredProperty("community.service.errors.bad-community-ref-length"),
                                e.getRequiredProperty("community.service.rules.community-ref.min-length"),
                                e.getRequiredProperty("community.service.rules.community-ref.max-length"))
                )));
    }

    @Test
    void testAttemptDuplicateLocator() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("community.service.endpoints.community.path"));

        final UUID ownerId = UUID.randomUUID();

        final String duplicateLocator = "hello-world2";

        final var request = new UpdateCommunityRequest(
                1L,
                duplicateLocator,
                null,
                null);

        when(userProfileService.authenticate(any())).thenReturn(ownerId);

        when(communityRepo.findCommunity(any(Long.class)))
                .thenReturn(Optional.of(new Community(request.communityId(), null, null, ownerId, null, 1)));

        doThrow(new ValidationException("communityLocator",
                duplicateLocator,
                e.getRequiredProperty("community.service.errors.ref-already-taken")
        )).when(communityRepo).updateCommunityAttrs(request.communityId(), request);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization", DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(e.getRequiredProperty("community.service.errors.ref-already-taken"))))
        ;
    }
}

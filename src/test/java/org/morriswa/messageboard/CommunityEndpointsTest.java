package org.morriswa.messageboard;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.enumerated.RequestField;
import org.morriswa.messageboard.model.CreateCommunityRequest;
import org.morriswa.messageboard.model.UpdateCommunityRequest;
import org.morriswa.messageboard.exception.NoRegisteredUserException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.service.UserProfileService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommunityEndpointsTest extends MessageboardTest {

    @MockBean private UserProfileService userProfileService;

    @Test
    void testUserShouldBeRegisteredToCreateCommunity() throws Exception {

        when(userProfileService.authenticate(any()))
                .thenThrow(new NoRegisteredUserException(e.getRequiredProperty("user-profile.service.errors.missing-user")));

        final CreateCommunityRequest body
                = new CreateCommunityRequest("hello-world","Hello World!");

        hit("community","community", HttpMethod.POST, body)
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("user-profile.service.errors.missing-user"))))
                .andExpect(jsonPath("$.error",
                        Matchers.is(NoRegisteredUserException.class.getSimpleName())))
        ;
    }

    @Test
    void testCreateCommunity() throws Exception {

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final CreateCommunityRequest body
                = new CreateCommunityRequest("hello-world","Hello World!");

        hit("community", "community", HttpMethod.POST, body)
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.message",
                        Matchers.is(e.getRequiredProperty("community.service.endpoints.community.messages.post"))))
        ;
    }

    @Test
    void testCreateCommunityBadLocator() throws Exception {

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final CreateCommunityRequest body
                = new CreateCommunityRequest("#hello-world","Hello World!");

        hit("community","community", HttpMethod.POST, body)
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(e.getRequiredProperty("community.service.errors.bad-community-ref"))))
        ;
    }

    @Test
    void testCreateCommunityShortLocator() throws Exception {

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        final CreateCommunityRequest body =
                new CreateCommunityRequest("hi","Hello World!");

        hit("community","community", HttpMethod.POST, body)
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

        final String DUPLICATE_NAME = "hello-world";

        when(userProfileService.authenticate(any())).thenReturn(UUID.randomUUID());

        doThrow(new ValidationException(
                "communityRef",
                RequestField.REQUIRED,
                DUPLICATE_NAME
                ,e.getRequiredProperty("community.service.errors.ref-already-taken")))
            .when(communityRepo)
            .createNewCommunity(any(),any());

        final CreateCommunityRequest body = new CreateCommunityRequest(DUPLICATE_NAME,"Hello World!");

        hit("community","community", HttpMethod.POST, body)
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(e.getRequiredProperty("community.service.errors.ref-already-taken"))))
        ;
    }

    @Test
    void testUpdateNoCommunity() throws Exception {

        final UUID ownerId = UUID.randomUUID();

        when(userProfileService.authenticate(any())).thenReturn(ownerId);

        when(communityRepo.findCommunity(any(Long.class)))
                .thenReturn(Optional.of(new Community(1L, null, null, ownerId, null, 1)));

        final var request = new UpdateCommunityRequest(
                null,
                "hello-world",
                null,
                null);

        hit("community","community", HttpMethod.PATCH, request)
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(e.getRequiredProperty("common.service.errors.missing-required-field"))))
                .andExpect(jsonPath("$.stack[0].field",
                        Matchers.is("communityId")))
                .andExpect(jsonPath("$.stack[0].status",
                        Matchers.is("REQUIRED")))
        ;
    }

    @Test
    void testUpdateCommunityNothing() throws Exception {

        final UUID ownerId = UUID.randomUUID();

        when(userProfileService.authenticate(any())).thenReturn(ownerId);

        when(communityRepo.findCommunity(any(Long.class))).thenReturn(Optional.of(new Community(1L, null, null, ownerId, null, 1)));

        final var request = new UpdateCommunityRequest(
                1L,
                null,
                null,
                null);

        hit("community","community", HttpMethod.PATCH, request)
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(e.getRequiredProperty("common.service.errors.missing-optional-fields"))))
        ;
    }

    @Test
    void testUpdateJustLocator() throws Exception {

        final UUID ownerId = UUID.randomUUID();

        when(userProfileService.authenticate(any())).thenReturn(ownerId);

        when(communityRepo.findCommunity(any(Long.class))).thenReturn(Optional.of(new Community(1L, null, null, ownerId, null, 1)));

        final var request = new UpdateCommunityRequest(
                1L,
                "hello-world2",
                null,
                null);

        hit("community","community", HttpMethod.PATCH, request)
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.message",
                        Matchers.is(e.getRequiredProperty("community.service.endpoints.community.messages.patch"))))
        ;
    }

    @Test
    void testUpdateBadLocator() throws Exception {

        final UUID ownerId = UUID.randomUUID();

        when(userProfileService.authenticate(any())).thenReturn(ownerId);

        when(communityRepo.findCommunity(any(Long.class))).thenReturn(Optional.of(new Community(1L, null, null, ownerId, null, 1)));

        final var request = new UpdateCommunityRequest(
                1L,
                "-hello-world2",
                null,
                null);

        hit("community","community", HttpMethod.PATCH, request)
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(e.getRequiredProperty("community.service.errors.bad-community-ref"))))
        ;
    }

    @Test
    void testUpdateLongLocator() throws Exception {

        final UUID ownerId = UUID.randomUUID();

        when(userProfileService.authenticate(any())).thenReturn(ownerId);

        when(communityRepo.findCommunity(any(Long.class))).thenReturn(Optional.of(new Community(1L, null, null, ownerId, null, 1)));

        final var request = new UpdateCommunityRequest(
                1L,
                "hello-01234567891123456789hello",
                null,
                null);

        hit("community","community", HttpMethod.PATCH, request)
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
                RequestField.REQUIRED,
                duplicateLocator,
                e.getRequiredProperty("community.service.errors.ref-already-taken")
        )).when(communityRepo).updateCommunityAttrs(request.communityId(), request);

        hit("community","community", HttpMethod.PATCH, request)
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown"))))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(e.getRequiredProperty("community.service.errors.ref-already-taken"))))
        ;
    }
}

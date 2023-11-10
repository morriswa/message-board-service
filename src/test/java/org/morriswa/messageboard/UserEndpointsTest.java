package org.morriswa.messageboard;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.entity.User;
import org.morriswa.messageboard.model.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserEndpointsTest extends MessageboardTest {

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
    void testGetUserProfileEndpoint() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user.path"));


        final User exampleUser = getExampleUser();

        when(userProfileDao.getUser(any(String.class))).thenReturn(Optional.of(exampleUser));

        this.mockMvc.perform(MockMvcRequestBuilders
                .get(targetUrl)
                .header("Authorization",DEFAULT_TOKEN))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.payload.displayName", Matchers.is(exampleUser.getDisplayName())))
            .andExpect(jsonPath("$.payload.userId", Matchers.equalTo(exampleUser.getUserId().toString())))
            .andExpect(jsonPath("$.payload.userProfileImage", Matchers.notNullValue()))
        ;
    }

    @Test
    void testGetUserProfileEndpointWithNoRegisteredUser() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user.path"));

        when(userProfileDao.getUser(any(String.class))).thenReturn(Optional.empty());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(targetUrl)
                        .header("Authorization",DEFAULT_TOKEN))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message",Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.missing-user"))))
        ;
    }

    @Test
    void testRegisterUserEndpoint() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user.path"));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl)
                        .header("Authorization",DEFAULT_TOKEN)
                        .param("email", TEST_EMAIL)
                        .param("displayName",DISPLAY_NAME))

                .andExpect(status().is(200))
                .andExpect(jsonPath("$.message", Matchers.notNullValue()))
        ;
    }

    @Test
    void testRegisterUserEndpointWithInvalidDisplayName() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user.path"));

        final String badDisplayName = "display$Name";

        this.mockMvc.perform(MockMvcRequestBuilders
                    .post(targetUrl)
                        .header("Authorization",DEFAULT_TOKEN)
                        .param("email", TEST_EMAIL)
                        .param("displayName",badDisplayName))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.bad-display-name")
                )))
                .andExpect(jsonPath("$.stack[0].rejectedValue", Matchers.is(
                        badDisplayName
                )))
        ;
    }

    @Test
    void testRegisterUserEndpointWithDuplicateDisplayName() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user.path"));

        final String duplicateDisplayName = "duplicateDisplayName";

        doThrow(new ValidationException("displayName", duplicateDisplayName,
                e.getRequiredProperty("user-profile.service.errors.display-name-already-exists")))
        .when(userProfileDao).createNewUser(any());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl)
                        .header("Authorization",DEFAULT_TOKEN)
                        .param("email",TEST_EMAIL)
                        .param("displayName",DISPLAY_NAME))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.display-name-already-exists")
                )))
                .andExpect(jsonPath("$.stack[0].rejectedValue", Matchers.is(
                        duplicateDisplayName
                )))
        ;
    }

    @Test
    void testUpdateUserDisplayNameEndpointWithInvalidDisplayName() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user-profile-displayname.path"));

        final String badDisplayName = "display$Name";

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization",DEFAULT_TOKEN)
                        .param("displayName",badDisplayName))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.bad-display-name")
                )))
                .andExpect(jsonPath("$.stack[0].rejectedValue", Matchers.is(
                        badDisplayName
                )))
        ;
    }

    @Test
    void testUpdateUserDisplayNameEndpointWithDuplicateDisplayName() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user-profile-displayname.path"));

        final String newDisplayName = "newDisplayName";

        when(userProfileDao.getUser(any(String.class))).thenReturn(Optional.of(getExampleUser()));

        doThrow(new ValidationException("displayName",newDisplayName,
                e.getRequiredProperty("user-profile.service.errors.display-name-already-exists")))
        .when(userProfileDao).updateUserDisplayName(any(), any());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization",DEFAULT_TOKEN)
                        .param("displayName",newDisplayName))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.display-name-already-exists")
                )))
                .andExpect(jsonPath("$.stack[0].rejectedValue", Matchers.is(
                        newDisplayName
                )))

        ;
    }


    @Test
    void testUpdateUserDisplayNameEndpointWithLongDisplayName() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user-profile-displayname.path"));

        final String longDisplayName = "012345678901234567890123456789012345";

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization",DEFAULT_TOKEN)
                        .param("displayName",longDisplayName))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.bad-display-name-length")
                )))
                .andExpect(jsonPath("$.stack[0].rejectedValue", Matchers.is(
                        longDisplayName
                )))
        ;
    }

    @Test
    void testUpdateUserDisplayNameEndpointWithShortDisplayName() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user-profile-displayname.path"));

        final String shortDisplayName = "01";

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization",DEFAULT_TOKEN)
                        .param("displayName",shortDisplayName))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.bad-display-name-length")
                )))
                .andExpect(jsonPath("$.stack[0].rejectedValue", Matchers.is(
                        shortDisplayName
                )))

        ;
    }
}

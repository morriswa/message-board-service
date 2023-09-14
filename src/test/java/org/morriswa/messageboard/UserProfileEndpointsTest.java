package org.morriswa.messageboard;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.entity.User;
import org.morriswa.messageboard.model.UserRole;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserProfileEndpointsTest extends MessageboardTest {

    @Test
    void testGetUserProfileEndpoint() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user.path"));


        final User exampleUser = new User(UUID.randomUUID(),
                "abc",
                UserRole.DEFAULT,
                "displayName",
                "email@gmail.com");

        when(userProfileRepo.findUserByAuthZeroId(any())).thenReturn(Optional.of(exampleUser));

        this.mockMvc.perform(MockMvcRequestBuilders
                .get(targetUrl)
                .header("Authorization","Bearer token"))
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

        when(userProfileRepo.findUserByAuthZeroId(any())).thenReturn(Optional.empty());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(targetUrl)
                        .header("Authorization","Bearer token"))
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
                        .header("Authorization","Bearer token")
                        .param("email","email@gmail.com")
                        .param("displayName","displayName"))

                .andExpect(status().is(200))
                .andExpect(jsonPath("$.message", Matchers.notNullValue()))

        ;
    }

    @Test
    void testRegisterUserEndpointWithInvalidDisplayName() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user.path"));

        this.mockMvc.perform(MockMvcRequestBuilders
                    .post(targetUrl)
                        .header("Authorization","Bearer token")
                        .param("email","email@gmail.com")
                        .param("displayName","display$Name"))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.bad-display-name")
                )))

        ;
    }

    @Test
    void testRegisterUserEndpointWithDuplicateDisplayName() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user.path"));

        when(userProfileRepo.existsByDisplayName(any())).thenReturn(true);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl)
                        .header("Authorization","Bearer token")
                        .param("email","email@gmail.com")
                        .param("displayName","displayName"))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.display-name-already-exists")
                )))

        ;
    }

    @Test
    void testRegisterUserEndpointWithInvalidEmail() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user.path"));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl)
                        .header("Authorization","Bearer token")
                        .param("email","emailgmail.com")
                        .param("displayName","displayName"))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.notNullValue()))

        ;
    }

    @Test
    void testUpdateUserDisplayNameEndpointWithInvalidDisplayName() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user-profile-displayname.path"));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization","Bearer token")
                        .param("displayName","display$Name"))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.bad-display-name")
                )))

        ;
    }

    @Test
    void testUpdateUserDisplayNameEndpointWithLongDisplayName() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user-profile-displayname.path"));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization","Bearer token")
                        .param("displayName","012345678901234567890123456789012345"))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.bad-display-name-length")
                )))

        ;
    }

    @Test
    void testUpdateUserDisplayNameEndpointWithShortDisplayName() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user-profile-displayname.path"));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization","Bearer token")
                        .param("displayName","01"))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.bad-display-name-length")
                )))

        ;
    }
}

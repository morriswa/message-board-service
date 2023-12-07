package org.morriswa.messageboard;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.control.requestbody.NewUserRequestBody;
import org.morriswa.messageboard.enumerated.UserRole;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private String getMinimumAgeDate(int offsetYears) {
        final int MINIMUM_AGE = Integer.parseInt(e.getRequiredProperty("common.minimum-age"));

        DateTimeFormatter format = DateTimeFormatter.ofPattern(
                e.getRequiredProperty("common.date-format")
        );

        final LocalDate todaysDate = LocalDate.now();

        final LocalDate offsetDate = todaysDate.minusYears(MINIMUM_AGE + offsetYears);

        return offsetDate.format(format);
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
                .andExpect(jsonPath("$.description",Matchers.is(
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new NewUserRequestBody(DISPLAY_NAME, getMinimumAgeDate(2)))))

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new NewUserRequestBody(badDisplayName, "2000-1-1"))))


                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description", Matchers.is(
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
    void testRegisterUserEndpointWithYoungBirthday() throws Exception {

        final String targetUrl = String.format("/%s%s",
                e.getRequiredProperty("server.path"),
                e.getRequiredProperty("user-profile.service.endpoints.user.path"));

        final String date = getMinimumAgeDate(-1);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(targetUrl)
                        .header("Authorization",DEFAULT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new NewUserRequestBody("displayName", date))))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",
                        Matchers.is(e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message",
                        Matchers.is(String.format(e.getRequiredProperty("user-profile.service.errors.too-young"),
                                e.getRequiredProperty("common.minimum-age")))))
                .andExpect(jsonPath("$.stack[0].rejectedValue", Matchers.is(date)))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new NewUserRequestBody(DISPLAY_NAME, "2000-1-1"))))


                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description", Matchers.is(
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
                .andExpect(jsonPath("$.description", Matchers.is(
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

        when(userProfileDao.getUserId(any(String.class))).thenReturn(Optional.of(getExampleUser().getUserId()));

        doThrow(new ValidationException("displayName",newDisplayName,
                e.getRequiredProperty("user-profile.service.errors.display-name-already-exists")))
        .when(userProfileDao).updateUserDisplayName(any(), any());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch(targetUrl)
                        .header("Authorization",DEFAULT_TOKEN)
                        .param("displayName",newDisplayName))

                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description", Matchers.is(
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
                .andExpect(jsonPath("$.description", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        String.format(
                                e.getRequiredProperty("user-profile.service.errors.bad-display-name-length"),
                                e.getRequiredProperty("user-profile.service.rules.display-name.min-length"),
                                e.getRequiredProperty("user-profile.service.rules.display-name.max-length")
                ))))
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
                .andExpect(jsonPath("$.description", Matchers.is(
                        e.getRequiredProperty("common.service.errors.validation-exception-thrown")
                )))
                .andExpect(jsonPath("$.stack[0].message", Matchers.is(
                        String.format(
                        e.getRequiredProperty("user-profile.service.errors.bad-display-name-length"),
                        e.getRequiredProperty("user-profile.service.rules.display-name.min-length"),
                        e.getRequiredProperty("user-profile.service.rules.display-name.max-length")
                ))))
                .andExpect(jsonPath("$.stack[0].rejectedValue", Matchers.is(
                        shortDisplayName
                )))

        ;
    }
}

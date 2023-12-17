package org.morriswa.messageboard;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.control.requestbody.NewUserRequestBody;
import org.morriswa.messageboard.enumerated.UserRole;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.User;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserEndpointsTest extends MessageboardTest {

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

        final User exampleUser = getExampleUser();

        when(userProfileDao.getUser(any(String.class))).thenReturn(Optional.of(exampleUser));

        hit("user-profile","user",HttpMethod.GET)
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.payload.displayName", Matchers.is(exampleUser.getDisplayName())))
            .andExpect(jsonPath("$.payload.userId", Matchers.equalTo(exampleUser.getUserId().toString())))
            .andExpect(jsonPath("$.payload.userProfileImage", Matchers.notNullValue()))
        ;
    }

    @Test
    void testGetUserProfileEndpointWithNoRegisteredUser() throws Exception {

        when(userProfileDao.getUser(any(String.class))).thenReturn(Optional.empty());

        hit("user-profile","user",HttpMethod.GET)
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description",Matchers.is(
                        e.getRequiredProperty("user-profile.service.errors.missing-user"))))
        ;
    }

    @Test
    void testRegisterUserEndpoint() throws Exception {

        hit(
            "user-profile", "user", HttpMethod.POST,
            new NewUserRequestBody(DISPLAY_NAME, getMinimumAgeDate(2))
        )
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.message", Matchers.notNullValue()))
        ;
    }

    @Test
    void testRegisterUserEndpointWithInvalidDisplayName() throws Exception {

        final String badDisplayName = "display$Name";

        hit("user-profile","user",HttpMethod.POST,
                new NewUserRequestBody(badDisplayName, "2000-1-1")
        )
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

        final String date = getMinimumAgeDate(-1);

        hit(
        "user-profile","user", HttpMethod.POST, new NewUserRequestBody("displayName", date)
        )
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

        final String duplicateDisplayName = "duplicateDisplayName";

        doThrow(new ValidationException("displayName", duplicateDisplayName,
                e.getRequiredProperty("user-profile.service.errors.display-name-already-exists")))
        .when(userProfileDao).createNewUser(any());

        hit("user-profile","user",HttpMethod.POST,
                new NewUserRequestBody(DISPLAY_NAME, "2000-1-1")
        )
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

        final String badDisplayName = "display$Name";

        hit(
                "user-profile", "user-profile-displayname",
                Map.of("displayName",badDisplayName),
                HttpMethod.PATCH
        )
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

        final String newDisplayName = "newDisplayName";

        when(userProfileDao.getUserId(any(String.class))).thenReturn(Optional.of(getExampleUser().getUserId()));

        doThrow(new ValidationException("displayName",newDisplayName,
                e.getRequiredProperty("user-profile.service.errors.display-name-already-exists")))
        .when(userProfileDao).updateUserDisplayName(any(), any());

        hit(
                "user-profile", "user-profile-displayname",
                Map.of("displayName",newDisplayName),
                HttpMethod.PATCH
        )
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

        final String longDisplayName = "012345678901234567890123456789012345";

        hit(
                "user-profile", "user-profile-displayname",
                Map.of("displayName",longDisplayName),
                HttpMethod.PATCH
        )
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

        final String shortDisplayName = "01";

        hit(
                "user-profile", "user-profile-displayname",
                Map.of("displayName",shortDisplayName),
                HttpMethod.PATCH
        )
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

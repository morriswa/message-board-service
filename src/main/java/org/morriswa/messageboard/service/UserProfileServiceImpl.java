package org.morriswa.messageboard.service;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.*;
import org.morriswa.messageboard.entity.User;
import org.morriswa.messageboard.repo.UserProfileRepo;
import org.morriswa.messageboard.service.util.ProfileImageService;
import org.morriswa.messageboard.validation.UserProfileServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service @Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final Environment e;
    private final UserProfileRepo userProfileRepo;
    private final UserProfileServiceValidator validator;
    private final ProfileImageService profileImageService;

    @Autowired
    public UserProfileServiceImpl(
            Environment e, UserProfileRepo userProfileRepo,
            UserProfileServiceValidator validator,
            ProfileImageService profileImageService) {
        this.e = e;
        this.userProfileRepo = userProfileRepo;
        this.validator = validator;
        this.profileImageService = profileImageService;
    }

    private UserProfileResponse buildUserProfileResponse(User user) {
        var userProfileImage = profileImageService.getSignedUserProfileImage(user.getUserId());

        return new UserProfileResponse(user,userProfileImage);
    }

    private void displayNameIsAvailableOrThrow(String displayName) throws ValidationException {
        if (userProfileRepo.existsByDisplayName(displayName))
            throw new ValidationException(
                "displayName",
                displayName,
                e.getRequiredProperty("user-profile.service.errors.display-name-already-exists"));
    }

    private String extractEmailFromJwt(JwtAuthenticationToken token) {
        String email; // get email logic for new user
        if (    // if the application is running a test
                e.getActiveProfiles()[0].equals("test")
                ||      // or the application is running in a local development environment
                (       System.getenv("APPCONFIG_ENV_ID").equals("local")
                        && // and the user has not included an email in their JWT
                        !token.getTokenAttributes().containsKey("email"))
        ) { // in local/test environments, this value may be filled by local property
            email = e.getProperty("testemail");
        } else
            // in most cases this value should come from users token
            email = String.valueOf(token.getTokenAttributes().get("email"));
        return email;
    }

    @Override
    public UserProfileResponse authenticateAndGetUserProfile(JwtAuthenticationToken token) throws BadRequestException {
        var user = authenticateAndGetUserEntity(token);

        return buildUserProfileResponse(user);
    }

    @Override
    public UserProfileResponse getUserProfile(UUID userId) throws BadRequestException {
        var user = userProfileRepo.findUserByUserId(userId)
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));

        return buildUserProfileResponse(user);
    }

    @Override
    public User authenticateAndGetUserEntity(JwtAuthenticationToken token) throws BadRequestException {
        return userProfileRepo.findUserByAuthZeroId(token.getName())
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));
    }

    @Override
    public String createNewUser(JwtAuthenticationToken token, String displayName) throws ValidationException {

        validator.validateDisplayNameOrThrow(displayName);

        displayNameIsAvailableOrThrow(displayName);

        var newUser = User.builder();
        newUser.authZeroId(token.getName());
        newUser.email(extractEmailFromJwt(token));
        newUser.displayName(displayName);
        newUser.role(UserRole.DEFAULT);

        var user = newUser.build();
        validator.validateBeanOrThrow(user);

        userProfileRepo.save(user);

        return user.getDisplayName();
    }

    @Override
    public void updateUserProfileImage(JwtAuthenticationToken token, UploadImageRequest request) throws BadRequestException, IOException {

        var user = authenticateAndGetUserEntity(token);

        profileImageService.updateUserProfileImage(user.getUserId(), request);
    }

    @Override
    public URL getUserProfileImage(JwtAuthenticationToken token) throws BadRequestException {

        var user = authenticateAndGetUserEntity(token);

        return profileImageService.getSignedUserProfileImage(user.getUserId());
    }

    @Override
    public void updateUserProfileDisplayName(JwtAuthenticationToken token, String requestedDisplayName) throws BadRequestException, ValidationException {
        // ensure display name follows basic rules
        this.validator.validateDisplayNameOrThrow(requestedDisplayName);

        var user = authenticateAndGetUserEntity(token);

        displayNameIsAvailableOrThrow(requestedDisplayName);

        // update display name
        user.setDisplayName(requestedDisplayName);

        // and validate the user is valid
        this.validator.validateBeanOrThrow(user);

        // save changes
        this.userProfileRepo.save(user);
    }

}

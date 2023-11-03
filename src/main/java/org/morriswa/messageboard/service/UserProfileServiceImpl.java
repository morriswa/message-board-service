package org.morriswa.messageboard.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.*;
import org.morriswa.messageboard.dao.UserProfileDao;
import org.morriswa.messageboard.stores.ProfileImageStoreImpl;
import org.morriswa.messageboard.validation.UserProfileServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

@Service @Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final Environment e;
    private final UserProfileDao userProfileDao;
    private final UserProfileServiceValidator validator;
    private final ProfileImageStoreImpl profileImageStoreImpl;

    @Autowired
    public UserProfileServiceImpl(
            Environment e, UserProfileDao userProfileDao,
            UserProfileServiceValidator validator,
            ProfileImageStoreImpl profileImageStoreImpl) {
        this.e = e;
        this.userProfileDao = userProfileDao;
        this.validator = validator;
        this.profileImageStoreImpl = profileImageStoreImpl;
    }

    private void displayNameIsAvailableOrThrow(String displayName) throws ValidationException {
        if (userProfileDao.existsByDisplayName(displayName))
            throw new ValidationException(
                "displayName",
                displayName,
                e.getRequiredProperty("user-profile.service.errors.display-name-already-exists"));
    }

    private String extractEmailFromJwt(JwtAuthenticationToken token) {
        return String.valueOf(token.getTokenAttributes().get("email"));
    }

    @Override
    public UserProfile authenticateAndGetUserProfile(JwtAuthenticationToken token) throws BadRequestException {
        var user = userProfileDao.getUserProfile(token.getName())
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));

        user.setUserProfileImage(
                profileImageStoreImpl.getSignedUserProfileImage(user.getUserId())
        );

        return user;
    }

    @Override
    public UUID authenticate(JwtAuthenticationToken token) throws BadRequestException {
        return userProfileDao.getUserId(token.getName())
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));
    }

    @Override
    public UserProfile getUserProfile(UUID userId) throws BadRequestException {
        var user = userProfileDao.getUserProfile(userId)
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));

        user.setUserProfileImage(
                profileImageStoreImpl.getSignedUserProfileImage(user.getUserId())
        );

        return user;
    }

    @Override
    public String createNewUser(JwtAuthenticationToken token, String displayName) throws ValidationException {

        validator.validateDisplayNameOrThrow(displayName);

        displayNameIsAvailableOrThrow(displayName);

        var newUser = CreateUserRequest.builder();
        newUser.authZeroId(token.getName());
        newUser.email(extractEmailFromJwt(token));
        newUser.displayName(displayName);
        newUser.role(UserRole.DEFAULT);

        var user = newUser.build();
        validator.validateBeanOrThrow(user);

        userProfileDao.createNewUser(user);

        return user.getDisplayName();
    }

    @Override
    public void updateUserProfileImage(JwtAuthenticationToken token, UploadImageRequest request) throws BadRequestException, IOException {

        var user = authenticateAndGetUserProfile(token);

        profileImageStoreImpl.updateUserProfileImage(user.getUserId(), request);
    }

    @Override
    public void updateUserProfileDisplayName(JwtAuthenticationToken token, String requestedDisplayName) throws BadRequestException, ValidationException {
        // ensure display name follows basic rules
        this.validator.validateDisplayNameOrThrow(requestedDisplayName);

        UserProfile user = authenticateAndGetUserProfile(token);

        displayNameIsAvailableOrThrow(requestedDisplayName);

        // save changes
        this.userProfileDao.updateUserDisplayName(user.getUserId(), user.getDisplayName());
    }

}

package org.morriswa.messageboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.entity.User;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.*;
import org.morriswa.messageboard.dao.UserProfileDao;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.morriswa.messageboard.model.responsebody.UserProfile;
import org.morriswa.messageboard.model.validatedrequest.CreateUserRequest;
import org.morriswa.messageboard.stores.ProfileImageStoreImpl;
import org.morriswa.messageboard.validation.UserProfileServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
        var user = userProfileDao.getUser(token.getName())
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));

        var profileImage = profileImageStoreImpl.getSignedUserProfileImage(user.getUserId());

        return new UserProfile(user, profileImage);
    }

    @Override
    public User authenticateAndGetUser(JwtAuthenticationToken token) throws BadRequestException {
        return userProfileDao.getUser(token.getName())
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));
    }

    @Override
    public UUID authenticate(JwtAuthenticationToken token) throws BadRequestException {
        return userProfileDao.getUserId(token.getName())
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));
    }

    @Override
    public UserProfile getUserProfile(UUID userId) throws BadRequestException {
        var user = userProfileDao.getUser(userId)
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));

        var profileImage =
                profileImageStoreImpl.getSignedUserProfileImage(user.getUserId());

        return new UserProfile(user, profileImage);
    }

    @Override
    public String createNewUser(JwtAuthenticationToken token, String displayName) throws ValidationException, JsonProcessingException {

        validator.validateDisplayNameOrThrow(displayName);

//        displayNameIsAvailableOrThrow(displayName);

        var newUser = new CreateUserRequest(
                token.getName(),
                extractEmailFromJwt(token),
                displayName,
                UserRole.DEFAULT);

        validator.validateBeanOrThrow(newUser);

        userProfileDao.createNewUser(newUser);

        return newUser.getDisplayName();
    }

    @Override
    public void updateUserProfileImage(JwtAuthenticationToken token, UploadImageRequest request) throws BadRequestException, IOException {

        var userId = authenticate(token);

        validator.validateBeanOrThrow(request);

        profileImageStoreImpl.updateUserProfileImage(userId, request);
    }

    @Override
    public void updateUserProfileDisplayName(JwtAuthenticationToken token, String requestedDisplayName) throws BadRequestException, ValidationException {
        // ensure display name follows basic rules
        this.validator.validateDisplayNameOrThrow(requestedDisplayName);

        User user = authenticateAndGetUser(token);

//        if (!user.getDisplayName().equals(requestedDisplayName))
//            displayNameIsAvailableOrThrow(requestedDisplayName);

        // save changes
        this.userProfileDao.updateUserDisplayName(user.getUserId(), requestedDisplayName);
    }

}

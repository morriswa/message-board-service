package org.morriswa.messageboard.service;

import lombok.extern.slf4j.Slf4j;
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
    public User createNewUser(JwtAuthenticationToken token, String email, String displayName) throws BadRequestException, ValidationException {

        validator.validateDisplayNameOrThrow(displayName);

        if (userProfileRepo.existsByDisplayName(displayName))
            throw new BadRequestException(
                e.getRequiredProperty("user-profile.service.errors.display-name-already-exists"));

        var newUser = User.builder();
        newUser.authZeroId(token.getName());
        newUser.email(email);
        newUser.displayName(displayName);
        newUser.role(UserRole.DEFAULT);

        var user = newUser.build();
        validator.validateBeanOrThrow(user);

        userProfileRepo.save(user);

        return user;
    }

    @Override
    public void updateUserProfileImage(JwtAuthenticationToken token, UpdateProfileImageRequest request) throws BadRequestException, IOException {

        var user = authenticateAndGetUserEntity(token);

        profileImageService.uploadImageToS3(user.getUserId(),request);
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

        // update display name
        user.setDisplayName(requestedDisplayName);
        // and validate the user is valid
        this.validator.validateBeanOrThrow(user);

        try { // attempt to save changes
            this.userProfileRepo.save(user);
        } catch (Exception ex) { // assume any error was caused by data integrity violation and stop request
            throw new BadRequestException(e.getProperty("user-profile.service.errors.display-name-already-exists"));
        }
    }


}

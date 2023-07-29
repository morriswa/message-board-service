package org.morriswa.messageboard.service;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.BadRequestException;
import org.morriswa.messageboard.entity.User;
import org.morriswa.messageboard.model.UpdateProfileImageRequest;
import org.morriswa.messageboard.model.UserProfileResponse;
import org.morriswa.messageboard.model.UserRole;
import org.morriswa.messageboard.repo.UserProfileRepo;
import org.morriswa.messageboard.service.util.ProfileImageService;
import org.morriswa.messageboard.validation.UserProfileServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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

    private User getUserEntityByAuthZeroIdOrThrow(String authZeroId) throws BadRequestException {
        return userProfileRepo.findUserByAuthZeroId(authZeroId)
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));
    }

    @Override
    public UserProfileResponse getUserProfile(String authZeroId) throws BadRequestException {
        var user = getUserEntityByAuthZeroIdOrThrow(authZeroId);

        var userProfileImage = profileImageService.getSignedUserProfileImage(user.getUserId());

        var userProfileResponse = new UserProfileResponse(user,userProfileImage);

        return userProfileResponse;
    }

    @Override
    public UUID getUserId(String authZeroId) throws BadRequestException {
        var user = getUserEntityByAuthZeroIdOrThrow(authZeroId);
        return user.getUserId();
    }

    @Override
    public User createNewUser(String authZeroId, String email, String displayName) {

        var newUser = User.builder();
        newUser.authZeroId(authZeroId);
        newUser.email(email);
        newUser.displayName(displayName);
        newUser.role(UserRole.DEFAULT);

        var user = newUser.build();
        validator.validateBeanOrThrow(user);

        userProfileRepo.save(user);

        return user;
    }

    @Override
    public void updateUserProfileImage(String name, UpdateProfileImageRequest request) throws BadRequestException, IOException {

        var user = userProfileRepo.findUserByAuthZeroId(name)
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));



        profileImageService.uploadImageToS3(user.getUserId(),request);
    }

    @Override
    public URL getUserProfileImage(String name) throws BadRequestException {
        var user = userProfileRepo.findUserByAuthZeroId(name)
                .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));

        return profileImageService.getSignedUserProfileImage(user.getUserId());
    }

    @Override
    public void updateUserProfileDisplayName(String authZeroId, String requestedDisplayName) throws BadRequestException {
        // ensure display name follows basic rules
        this.validator.validateDisplayNameOrThrow(requestedDisplayName);

        var user = userProfileRepo.findUserByAuthZeroId(authZeroId)
            .orElseThrow(()->new BadRequestException(e.getProperty("user-profile.service.errors.missing-user")));

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

package org.morriswa.messageboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.morriswa.messageboard.exception.NoRegisteredUserException;
import org.morriswa.messageboard.model.User;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.dao.UserProfileDao;
import org.morriswa.messageboard.model.UserUiProfile;
import org.morriswa.messageboard.enumerated.UserRole;
import org.morriswa.messageboard.model.UpdateUIProfileRequest;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.model.CreateUserRequest;
import org.morriswa.messageboard.store.ProfileImageStoreImpl;
import org.morriswa.messageboard.validation.UserProfileServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

import static org.morriswa.messageboard.util.Functions.blobTypeToImageFormat;

@Service
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

    private String extractEmailFromJwt(JwtAuthenticationToken token) {
        return String.valueOf(token.getTokenAttributes().get("email"));
    }

    @Override
    public User.Response authenticateAndGetUserProfile(JwtAuthenticationToken token) throws NoRegisteredUserException {
        var user = userProfileDao.getUser(token.getName())
                .orElseThrow(()->new NoRegisteredUserException(e.getProperty("user-profile.service.errors.missing-user")));

        var profileImage = profileImageStoreImpl.getSignedUserProfileImage(user.userId());

        return new User.Response(user, profileImage);
    }

    @Override
    public User authenticateAndGetUser(JwtAuthenticationToken token) throws NoRegisteredUserException {
        return userProfileDao.getUser(token.getName())
                .orElseThrow(()->new NoRegisteredUserException(e.getProperty("user-profile.service.errors.missing-user")));
    }

    @Override
    public UUID authenticate(JwtAuthenticationToken token) throws NoRegisteredUserException {
        return userProfileDao.getUserId(token.getName())
                .orElseThrow(()->new NoRegisteredUserException(e.getProperty("user-profile.service.errors.missing-user")));
    }

    @Override
    public User.Response getUserProfile(UUID userId) throws NoRegisteredUserException {
        var user = userProfileDao.getUser(userId)
                .orElseThrow(()->new NoRegisteredUserException(e.getProperty("user-profile.service.errors.missing-user")));

        var profileImage =
                profileImageStoreImpl.getSignedUserProfileImage(user.userId());

        return new User.Response(user, profileImage);
    }

    @Override
    public String registerUser(JwtAuthenticationToken token, CreateUserRequest.Body request) throws ValidationException, JsonProcessingException {

        validator.validate(request);

        DateTimeFormatter format = DateTimeFormatter.ofPattern(
                e.getRequiredProperty("common.date-format")
        );

        final LocalDate parsedDate = LocalDate.parse(request.birthdate(), format);

        var newUser = new CreateUserRequest(
                token.getName(),
                extractEmailFromJwt(token),
                parsedDate,
                request.displayName(),
                UserRole.DEFAULT);

        validator.validateBeanOrThrow(newUser);

        userProfileDao.createNewUser(newUser);

        return newUser.displayName();
    }

    @Override
    public void updateUserProfileImage(JwtAuthenticationToken token, MultipartFile image) throws NoRegisteredUserException, IOException {

        var userId = authenticate(token);

        validator.validateBeanOrThrow(image);

        Objects.requireNonNull(image.getContentType());

        profileImageStoreImpl.updateUserProfileImage(userId,
            new UploadImageRequest(
                image.getBytes(),
                blobTypeToImageFormat(image.getContentType())));
    }

    @Override
    public void updateUserProfileDisplayName(JwtAuthenticationToken token, String requestedDisplayName) throws NoRegisteredUserException, ValidationException {
        // ensure display name follows basic rules
        this.validator.validateDisplayNameOrThrow(requestedDisplayName);

        UUID user = authenticate(token);

        // save changes
        this.userProfileDao.updateUserDisplayName(user, requestedDisplayName);
    }

    @Override
    public UserUiProfile getUserUiProfile(JwtAuthenticationToken jwt) throws NoRegisteredUserException {
        var userId = authenticate(jwt);

        return userProfileDao.getUIProfile(userId);
    }

    @Override
    public void updateUserUiProfile(JwtAuthenticationToken jwt, UpdateUIProfileRequest request) throws NoRegisteredUserException {
        var userId = authenticate(jwt);

        userProfileDao.setUIProfile(userId, request);
    }

    @Override
    public URL getProfileImage(UUID userId) {
        return profileImageStoreImpl.getSignedUserProfileImage(userId);
    }

}

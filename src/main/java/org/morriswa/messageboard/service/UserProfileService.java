package org.morriswa.messageboard.service;

import org.morriswa.messageboard.model.*;
import org.morriswa.messageboard.exception.BadRequestException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.UUID;

/**
 * Service Interface to provided Vital User Actions to the Messageboard Application
 * Responsible for core tasks such as user authorization for actions throughout the application
 */
public interface UserProfileService {

    /**
     * Authenticates an Oauth2 user with provided token
     *
     * @param token to authenticate with
     * @return a full User Profile Response
     */
    User.Response authenticateAndGetUserProfile(JwtAuthenticationToken token) throws Exception;

    /**
     * Authenticates an Oauth2 user with provided token
     *
     * @param token to authenticate with
     * @return a User Response
     */
    User authenticateAndGetUser(JwtAuthenticationToken token) throws Exception;

    /**
     * Authenticates an Oauth2 user with provided token
     *
     * @param token to authenticate with
     * @return a UUID representing the user
     */
    UUID authenticate(JwtAuthenticationToken token) throws Exception;

    /**
     * <h1>NOT MEANT FOR AUTHENTICATING A USER</h1>
     * retrieves a FULL USER PROFILE by user-id
     *
     * @param userId UUID of user to retrieve
     * @return a full User Profile Response
     * @throws BadRequestException if the user cannot be found
     */
    User.Response getUserProfile(UUID userId) throws Exception;

    /**
     * Creates a new Messageboard User
     *
     * @param token to authenticate and register with
     * @param request containing important registration info
     * @return the display name that the user was registered with
     */
    String registerUser(JwtAuthenticationToken token, CreateUserRequest.Body request) throws Exception;

    /**
     * Updates a User's profile image
     *
     * @param token to authenticate user with
     * @param image representing new profile image
     */
    void updateUserProfileImage(JwtAuthenticationToken token, MultipartFile image) throws Exception;

    /**
     * Updates a user's display name
     *
     * @param token to authenticate with
     * @param requestedDisplayName user's requested new display name
     */
    void updateUserProfileDisplayName(JwtAuthenticationToken token, String requestedDisplayName) throws Exception;

    UserUiProfile getUserUiProfile(JwtAuthenticationToken jwt) throws Exception;

    void updateUserUiProfile(JwtAuthenticationToken jwt, UpdateUIProfileRequest request) throws Exception;

    URL getProfileImage(UUID userId);
}

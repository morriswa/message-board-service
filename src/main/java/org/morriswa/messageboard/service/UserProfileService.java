package org.morriswa.messageboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.entity.User;
import org.morriswa.messageboard.model.responsebody.UserProfile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
     * @throws BadRequestException if the user cannot be found or authenticated
     */
    UserProfile authenticateAndGetUserProfile(JwtAuthenticationToken token) throws BadRequestException;

    /**
     * Authenticates an Oauth2 user with provided token
     *
     * @param token to authenticate with
     * @return a User Response
     * @throws BadRequestException if the user cannot be found or authenticated
     */
    User authenticateAndGetUser(JwtAuthenticationToken token) throws BadRequestException;

    /**
     * Authenticates an Oauth2 user with provided token
     *
     * @param token to authenticate with
     * @return a UUID representing the user
     * @throws BadRequestException if the user cannot be found or authenticated
     */
    UUID authenticate(JwtAuthenticationToken token) throws BadRequestException;

    /**
     * <h1>NOT MEANT FOR AUTHENTICATING A USER</h1>
     * retrieves a FULL USER PROFILE by user-id
     *
     * @param userId UUID of user to retrieve
     * @return a full User Profile Response
     * @throws BadRequestException if the user cannot be found
     */
    UserProfile getUserProfile(UUID userId) throws BadRequestException;

    /**
     * Creates a new Messageboard User
     *
     * @param token to authenticate and register with
     * @param displayName user's requested display name
     * @return the display name that the user was registered with
     * @throws ValidationException if the displayName is poorly formatted or already taken
     */
    String createNewUser(JwtAuthenticationToken token, String displayName) throws BadRequestException, ValidationException, JsonProcessingException;

    /**
     * Updates a User's profile image
     *
     * @param token to authenticate user with
     * @param file representing new profile image
     * @throws BadRequestException if the user cannot be authenticated
     * @throws IOException if the image cannot be processed and uploaded successfully
     */
    void updateUserProfileImage(JwtAuthenticationToken token, MultipartFile file) throws BadRequestException, IOException;

    /**
     * Updates a user's display name
     *
     * @param token to authenticate with
     * @param requestedDisplayName user's requested new display name
     * @throws BadRequestException
     * @throws ValidationException
     */
    void updateUserProfileDisplayName(JwtAuthenticationToken token, String requestedDisplayName) throws BadRequestException, ValidationException;
}

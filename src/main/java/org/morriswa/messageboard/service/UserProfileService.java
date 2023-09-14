package org.morriswa.messageboard.service;

import org.morriswa.messageboard.model.BadRequestException;
import org.morriswa.messageboard.entity.User;
import org.morriswa.messageboard.model.UpdateProfileImageRequest;
import org.morriswa.messageboard.model.UserProfileResponse;
import org.morriswa.messageboard.model.ValidationException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public interface UserProfileService {

    UserProfileResponse authenticateAndGetUserProfile(JwtAuthenticationToken token) throws BadRequestException;

    UserProfileResponse getUserProfile(UUID userId) throws BadRequestException;

    User authenticateAndGetUserEntity(JwtAuthenticationToken token) throws BadRequestException;

    User createNewUser(JwtAuthenticationToken token, String email, String displayName) throws BadRequestException, ValidationException;

    void updateUserProfileImage(JwtAuthenticationToken token, UpdateProfileImageRequest request) throws BadRequestException, IOException;

    URL getUserProfileImage(JwtAuthenticationToken token) throws BadRequestException;

    void updateUserProfileDisplayName(JwtAuthenticationToken token, String requestedDisplayName) throws BadRequestException, ValidationException;
}

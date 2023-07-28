package org.morriswa.messageboard.service;

import org.morriswa.common.model.BadRequestException;
import org.morriswa.messageboard.entity.User;
import org.morriswa.messageboard.model.UpdateProfileImageRequest;
import org.morriswa.messageboard.model.UserProfileResponse;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public interface UserProfileService {
    UserProfileResponse getUserProfile(String authZeroId) throws BadRequestException;

    UUID getUserId(String authZeroId) throws BadRequestException;

    User createNewUser(String authZeroId, String email, String displayName);

    void updateUserProfileImage(String authZeroId, UpdateProfileImageRequest request) throws BadRequestException, IOException;

    URL getUserProfileImage(String authZeroId) throws BadRequestException;

    void updateUserProfileDisplayName(String authZeroId, String requestedDisplayName) throws BadRequestException;

}

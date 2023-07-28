package org.morriswa.userprofileservice.service;

import org.morriswa.common.model.BadRequestException;
import org.morriswa.userprofileservice.entity.User;
import org.morriswa.userprofileservice.model.UpdateProfileImageRequest;
import org.morriswa.userprofileservice.model.UserProfileResponse;

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

package org.morriswa.messageboard.control;

import org.morriswa.messageboard.model.BadRequestException;
import org.morriswa.messageboard.model.DefaultResponse;
import org.morriswa.messageboard.model.UpdateProfileImageRequest;
import org.morriswa.messageboard.model.ValidationException;
import org.morriswa.messageboard.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController @CrossOrigin
@RequestMapping("${server.path}")
public class UserProfileController {
    private final Environment e;
    private final UserProfileService userProfileService;

    @Autowired
    public UserProfileController(Environment e,
                                 UserProfileService userProfileService) {
        this.e = e;
        this.userProfileService = userProfileService;
    }

    @GetMapping("${user-profile.service.endpoints.user.path}")
    public ResponseEntity<?> getUserProfile(JwtAuthenticationToken jwt) throws BadRequestException {
        var user = userProfileService.authenticateAndGetUserProfile(jwt);
        return ResponseEntity.ok(new DefaultResponse<>(
                e.getRequiredProperty("user-profile.service.endpoints.user.messages.get"),
                user));
    }

    @PostMapping("${user-profile.service.endpoints.user.path}")
    public ResponseEntity<?> createNewUser(JwtAuthenticationToken jwt,
                                           @RequestParam String email,
                                           @RequestParam String displayName) throws BadRequestException, ValidationException {
        var newUser = userProfileService.createNewUser(jwt, email, displayName);
        return ResponseEntity.ok(new DefaultResponse<>(String.format(
            e.getRequiredProperty("user-profile.service.endpoints.user.messages.post"),
            newUser.getDisplayName())));
    }

    @GetMapping("${user-profile.service.endpoints.user-profile-image.path}")
    public ResponseEntity<?> getProfileImage(JwtAuthenticationToken jwt) throws BadRequestException {
        var profileImageUrl = userProfileService.getUserProfileImage(jwt);
        return ResponseEntity.ok(new DefaultResponse<>(
            e.getProperty("user-profile.service.endpoints.user-profile-image.messages.get"),
            profileImageUrl));
    }

    @PostMapping("${user-profile.service.endpoints.user-profile-image.path}")
    public ResponseEntity<?> updateUserProfileImage(JwtAuthenticationToken jwt,
                                                    @RequestBody UpdateProfileImageRequest request) throws BadRequestException, IOException {
        userProfileService.updateUserProfileImage(jwt, request);
        return ResponseEntity.ok(new DefaultResponse<>(
            e.getProperty("user-profile.service.endpoints.user-profile-image.messages.post")));
    }

    @PatchMapping("${user-profile.service.endpoints.user-profile-displayname.path}")
    public ResponseEntity<?> updateUserDisplayName(JwtAuthenticationToken jwt,
                                                    @RequestParam String displayName) throws BadRequestException, ValidationException {
        userProfileService.updateUserProfileDisplayName(jwt, displayName);
        return ResponseEntity.ok(new DefaultResponse<>(
                e.getProperty("user-profile.service.endpoints.user-profile-displayname.messages.patch")));
    }
}

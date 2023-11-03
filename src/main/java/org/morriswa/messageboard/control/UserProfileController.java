package org.morriswa.messageboard.control;

import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.responsebody.DefaultResponse;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
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
                                           @RequestParam String displayName) throws BadRequestException, ValidationException {
        var newUserDisplayName = userProfileService.createNewUser(jwt, displayName);
        return ResponseEntity.ok(new DefaultResponse<>(String.format(
            e.getRequiredProperty("user-profile.service.endpoints.user.messages.post"),
            newUserDisplayName)));
    }

    @PostMapping("${user-profile.service.endpoints.user-profile-image.path}")
    public ResponseEntity<?> updateUserProfileImage(JwtAuthenticationToken jwt,
                                                    @RequestBody UploadImageRequest request) throws BadRequestException, IOException {
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

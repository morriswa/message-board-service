package org.morriswa.messageboard.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.entity.UserUiProfile;
import org.morriswa.messageboard.model.requestbody.UpdateUIProfileRequest;
import org.morriswa.messageboard.model.responsebody.UserProfile;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.morriswa.messageboard.service.UserProfileService;
import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController @CrossOrigin
@RequestMapping("${server.path}")
public class UserProfileController {
    private final Environment e;
    private final UserProfileService userProfileService;
    private final HttpResponseFactoryImpl responseFactory;

    @Autowired
    public UserProfileController(Environment e,
                                 UserProfileService userProfileService,
                                 HttpResponseFactoryImpl responseFactory) {
        this.e = e;
        this.userProfileService = userProfileService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("${user-profile.service.endpoints.user.path}")
    public ResponseEntity<?> getUserProfile(JwtAuthenticationToken jwt) throws BadRequestException {
        UserProfile user = userProfileService.authenticateAndGetUserProfile(jwt);
        return responseFactory.getResponse(
            HttpStatus.OK,
            e.getRequiredProperty("user-profile.service.endpoints.user.messages.get"),
            user);
    }

    @PostMapping("${user-profile.service.endpoints.user.path}")
    public ResponseEntity<?> createNewUser(JwtAuthenticationToken jwt,
                                           @RequestParam String displayName) throws BadRequestException, ValidationException, JsonProcessingException {
        var newUserDisplayName = userProfileService.createNewUser(jwt, displayName);
        return responseFactory.getResponse(
            HttpStatus.OK,
            String.format(
                e.getRequiredProperty("user-profile.service.endpoints.user.messages.post"),
                newUserDisplayName));
    }

    @PostMapping("${user-profile.service.endpoints.user-profile-image.path}")
    public ResponseEntity<?> updateUserProfileImage(JwtAuthenticationToken jwt,
                                                    @RequestBody UploadImageRequest request) throws BadRequestException, IOException {
        userProfileService.updateUserProfileImage(jwt, request);
        return responseFactory.getResponse(
            HttpStatus.OK,
            e.getProperty("user-profile.service.endpoints.user-profile-image.messages.post"));
    }

    @PatchMapping("${user-profile.service.endpoints.user-profile-displayname.path}")
    public ResponseEntity<?> updateUserDisplayName(JwtAuthenticationToken jwt,
                                                    @RequestParam String displayName) throws BadRequestException, ValidationException {
        userProfileService.updateUserProfileDisplayName(jwt, displayName);
        return responseFactory.getResponse(
            HttpStatus.OK,
            e.getProperty("user-profile.service.endpoints.user-profile-displayname.messages.patch"));
    }

    @GetMapping("${user-profile.service.endpoints.user-ui.path}")
    public ResponseEntity<?> getUIProfile(JwtAuthenticationToken jwt) throws BadRequestException {
        UserUiProfile profile = userProfileService.getUserUiProfile(jwt);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("user-profile.service.endpoints.user-ui.messages.get"),
                profile);
    }

    @PatchMapping("${user-profile.service.endpoints.user-ui.path}")
    public ResponseEntity<?> updateUIProfile(JwtAuthenticationToken jwt,
                                             @RequestBody UpdateUIProfileRequest request) throws BadRequestException {
        userProfileService.updateUserUiProfile(jwt, request);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("user-profile.service.endpoints.user-ui.messages.patch"));
    }
}

package org.morriswa.messageboard.control;

import org.morriswa.messageboard.control.requestbody.NewUserRequestBody;
import org.morriswa.messageboard.model.UserUiProfile;
import org.morriswa.messageboard.control.requestbody.UpdateUIProfileRequest;
import org.morriswa.messageboard.model.UserProfileResponse;
import org.morriswa.messageboard.service.UserProfileService;
import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


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
    public ResponseEntity<?> getUserProfile(JwtAuthenticationToken jwt) throws Exception {
        UserProfileResponse user = userProfileService.authenticateAndGetUserProfile(jwt);
        return responseFactory.build(
            HttpStatus.OK,
            e.getRequiredProperty("user-profile.service.endpoints.user.messages.get"),
            user);
    }

    @PostMapping("${user-profile.service.endpoints.user.path}")
    public ResponseEntity<?> createNewUser(JwtAuthenticationToken jwt,
                                           @RequestBody NewUserRequestBody request) throws Exception {
        String newUserDisplayName = userProfileService.createNewUser(jwt, request);
        return responseFactory.build(
            HttpStatus.OK,
            String.format(
                e.getRequiredProperty("user-profile.service.endpoints.user.messages.post"),
                newUserDisplayName));
    }

    @PostMapping("${user-profile.service.endpoints.user-profile-image.path}")
    public ResponseEntity<?> updateUserProfileImage(JwtAuthenticationToken jwt,
                                                    @RequestPart MultipartFile image) throws Exception {
        userProfileService.updateUserProfileImage(jwt, image);
        return responseFactory.build(
            HttpStatus.OK,
            e.getProperty("user-profile.service.endpoints.user-profile-image.messages.post"));
    }

    @PatchMapping("${user-profile.service.endpoints.user-profile-displayname.path}")
    public ResponseEntity<?> updateUserDisplayName(JwtAuthenticationToken jwt,
                                                    @RequestParam String displayName) throws Exception {
        userProfileService.updateUserProfileDisplayName(jwt, displayName);
        return responseFactory.build(
            HttpStatus.OK,
            e.getProperty("user-profile.service.endpoints.user-profile-displayname.messages.patch"));
    }

    @GetMapping("${user-profile.service.endpoints.user-ui.path}")
    public ResponseEntity<?> getUIProfile(JwtAuthenticationToken jwt) throws Exception {
        UserUiProfile profile = userProfileService.getUserUiProfile(jwt);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("user-profile.service.endpoints.user-ui.messages.get"),
                profile);
    }

    @PatchMapping("${user-profile.service.endpoints.user-ui.path}")
    public ResponseEntity<?> updateUIProfile(JwtAuthenticationToken jwt,
                                             @RequestBody UpdateUIProfileRequest request) throws Exception {
        userProfileService.updateUserUiProfile(jwt, request);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("user-profile.service.endpoints.user-ui.messages.patch"));
    }
}

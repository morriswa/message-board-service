package org.morriswa.messageboard.service.util;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.validation.UserProfileServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service @Slf4j
public class NewProfileImageService extends GenericS3Store implements ProfileImageService {

    private final String DEFAULT_PROFILE_IMAGE_OBJECT_ID;
    private final String PROFILE_DIR;
    private final UserProfileServiceValidator validator;

    @Autowired
    NewProfileImageService(Environment e,
                           UserProfileServiceValidator validator,
                           ImageScaleService iss) {
        super(e, iss);
        this.validator = validator;
        this.PROFILE_DIR = e.getRequiredProperty("user-profile.service.stores.profile-images");
        this.DEFAULT_PROFILE_IMAGE_OBJECT_ID = e.getRequiredProperty("user-profile.service.stores.default-profile-image");
    }

    @Override
    public void updateUserProfileImage(UUID userId, UploadImageRequest request) throws IOException {
        validator.validateBeanOrThrow(request);

        int IMAGE_SCALE_FACTOR = Integer.parseInt(
                e.getRequiredProperty("user-profile.service.rules.user-profile-image-dimension"));

        var outfile = imageScaleService.getScaledImage(request, IMAGE_SCALE_FACTOR, IMAGE_SCALE_FACTOR);

        uploadObjectToS3(outfile, PROFILE_DIR+userId);
    }

    public URL getSignedUserProfileImage(UUID userId) {
        Date expiration = new Date();
        long expTimeMillis = Instant.now().toEpochMilli();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        String userProfileImageDest = PROFILE_DIR+userId.toString();

        if (!doesObjectExist(userProfileImageDest))
            return getSignedObjectUrl(DEFAULT_PROFILE_IMAGE_OBJECT_ID, expiration);

        return getSignedObjectUrl(userProfileImageDest, expiration);
    }
}

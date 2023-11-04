package org.morriswa.messageboard.stores;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.morriswa.messageboard.util.CustomS3UtilImpl;
import org.morriswa.messageboard.util.ImageScaleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Component @Slf4j
public class ProfileImageStoreImpl implements ProfileImageStore {
    private final int IMAGE_PX_WIDTH;
    private final String DEFAULT_PROFILE_IMAGE_OBJECT_ID;
    private final String PROFILE_DIR;
    private final ImageScaleUtil iss;
    private final CustomS3UtilImpl s3Store;

    @Autowired
    ProfileImageStoreImpl(Environment e,
                         ImageScaleUtil iss,
                         CustomS3UtilImpl s3Store) {
        this.PROFILE_DIR = e.getRequiredProperty("common.stores.profile-images");
        this.DEFAULT_PROFILE_IMAGE_OBJECT_ID = e.getRequiredProperty("common.static-content.default-profile-image");
        this.IMAGE_PX_WIDTH = Integer.parseInt(
                        e.getRequiredProperty("user-profile.service.rules.user-profile-image-dimension"));
        this.iss = iss;
        this.s3Store = s3Store;
    }

    @Override
    public void updateUserProfileImage(UUID userId, @Valid UploadImageRequest request) throws IOException {

        var image = iss.getScaledImage(request, IMAGE_PX_WIDTH, IMAGE_PX_WIDTH);

        s3Store.uploadToS3(image, request, PROFILE_DIR+userId);
    }

    @Override
    public URL getSignedUserProfileImage(UUID userId) {

        String userProfileImageDest = PROFILE_DIR+userId.toString();

        if (!s3Store.doesObjectExist(userProfileImageDest))
            return s3Store.getSignedObjectUrl(DEFAULT_PROFILE_IMAGE_OBJECT_ID, 60);

        return s3Store.getSignedObjectUrl(userProfileImageDest, 60);
    }
}

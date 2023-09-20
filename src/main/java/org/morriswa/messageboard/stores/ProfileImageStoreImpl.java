package org.morriswa.messageboard.stores;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.stores.util.CustomS3ServiceImpl;
import org.morriswa.messageboard.stores.util.ImageScaleService;
import org.morriswa.messageboard.validation.UserProfileServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service @Slf4j
public class ProfileImageStoreImpl implements ProfileImageStore {
    private final int IMAGE_PX_WIDTH;
    private final String DEFAULT_PROFILE_IMAGE_OBJECT_ID;
    private final String PROFILE_DIR;
    private final UserProfileServiceValidator validator;
    private final ImageScaleService iss;
    private final CustomS3ServiceImpl s3Store;

    @Autowired
    ProfileImageStoreImpl(Environment e,
                         UserProfileServiceValidator validator,
                         ImageScaleService iss,
                         CustomS3ServiceImpl s3Store) {
        this.validator = validator;
        this.PROFILE_DIR = e.getRequiredProperty("common.stores.profile-images");
        this.DEFAULT_PROFILE_IMAGE_OBJECT_ID = e.getRequiredProperty("common.static-content.default-profile-image");
        IMAGE_PX_WIDTH = Integer.parseInt(
                        e.getRequiredProperty("user-profile.service.rules.user-profile-image-dimension"));
        this.iss = iss;
        this.s3Store = s3Store;
    }

    @Override
    public void updateUserProfileImage(UUID userId, UploadImageRequest request) throws IOException {
        validator.validateBeanOrThrow(request);

        var outfile = iss.getScaledImage(request, IMAGE_PX_WIDTH, IMAGE_PX_WIDTH);

        s3Store.uploadObjectToS3(outfile, PROFILE_DIR+userId);
    }

    @Override
    public URL getSignedUserProfileImage(UUID userId) {

        String userProfileImageDest = PROFILE_DIR+userId.toString();

        if (!s3Store.doesObjectExist(userProfileImageDest))
            return s3Store.getSignedObjectUrl(DEFAULT_PROFILE_IMAGE_OBJECT_ID, 60);

        return s3Store.getSignedObjectUrl(userProfileImageDest, 60);
    }
}

package org.morriswa.messageboard.service.util;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.UploadImageRequest;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public interface ProfileImageService {
    /**
     * Uploads an Image to S3 and returns saved data
     *
     * @param request a valid ImageServiceRequest
     * @return the created resource
     */
    void uploadImageToS3(UUID userId, @Valid UploadImageRequest request) throws IOException;

    /**
     * Generates a URL of the Image signed for 30 minutes
     *
     * @param userId of the profile Image to retrieve
     * @return a URL valid for 30 minutes
     */
    URL getSignedUserProfileImage(UUID userId);
}

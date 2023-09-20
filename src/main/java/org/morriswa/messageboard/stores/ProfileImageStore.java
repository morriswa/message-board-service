package org.morriswa.messageboard.stores;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.UploadImageRequest;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public interface ProfileImageStore {
    /**
     * Uploads an Image
     *
     * @param request a valid ImageServiceRequest
     */
    void updateUserProfileImage(UUID userId, @Valid UploadImageRequest request) throws IOException;

    /**
     * Generates a URL of the Image signed for 30 minutes
     *
     * @param userId of the profile Image to retrieve
     * @return a URL valid for 30 minutes
     */
    URL getSignedUserProfileImage(UUID userId);
}

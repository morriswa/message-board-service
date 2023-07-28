package org.morriswa.messageboard.service.util;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import org.morriswa.common.model.UploadImageRequest;

public interface ImageResourceService {
    void uploadImage(UUID resourceID, UploadImageRequest request) throws IOException;
    URL retrievedImageResource(UUID resourceId);
}

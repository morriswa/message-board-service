package org.morriswa.messageboard.stores;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import org.morriswa.messageboard.model.UploadImageRequest;

public interface ImageStore {
    void uploadIndividualImage(UUID resourceID, UploadImageRequest request) throws IOException;
    URL retrieveImageResource(UUID resourceId);
}

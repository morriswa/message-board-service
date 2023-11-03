package org.morriswa.messageboard.stores;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;

public interface ImageStore {
    void uploadIndividualImage(UUID resourceID, @Valid UploadImageRequest request) throws IOException;
    URL retrieveImageResource(UUID resourceId);
}

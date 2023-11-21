package org.morriswa.messageboard.store;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;

public interface ResourceStore {
    void uploadIndividualImage(UUID resourceID, @Valid UploadImageRequest request) throws IOException;
    URL retrieveImageResource(UUID resourceId);
}

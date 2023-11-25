package org.morriswa.messageboard.store;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import jakarta.validation.Valid;
import org.morriswa.messageboard.validation.request.UploadImageRequest;

public interface ContentStore {
    void uploadIndividualImage(UUID resourceID, @Valid UploadImageRequest request) throws IOException;
    URL retrieveImageResource(UUID resourceId);
}

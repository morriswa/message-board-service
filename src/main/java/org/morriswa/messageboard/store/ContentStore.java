package org.morriswa.messageboard.store;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.Resource;
import org.morriswa.messageboard.validation.request.UploadImageRequest;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public interface ContentStore {

    void uploadIndividualImage(UUID resourceID, @Valid UploadImageRequest request) throws IOException;

    URL retrieveImageResource(UUID resourceId);

    void deleteResource(Resource resource);
}

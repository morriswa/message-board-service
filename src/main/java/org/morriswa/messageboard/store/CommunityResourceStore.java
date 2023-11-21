package org.morriswa.messageboard.store;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.responsebody.CommunityResponse;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;

import java.io.IOException;

public interface CommunityResourceStore {
    void setCommunityBanner(@Valid UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    void setCommunityIcon(@Valid UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    CommunityResponse.AllCommunityResourceURLs getAllCommunityResources(Long communityId);
}

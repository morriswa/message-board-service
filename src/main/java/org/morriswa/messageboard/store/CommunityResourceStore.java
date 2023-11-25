package org.morriswa.messageboard.store;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.CommunityResponse;
import org.morriswa.messageboard.validation.request.UploadImageRequest;

import java.io.IOException;

public interface CommunityResourceStore {
    void setCommunityBanner(@Valid UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    void setCommunityIcon(@Valid UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    CommunityResponse.AllCommunityResourceURLs getAllCommunityResources(Long communityId);
}

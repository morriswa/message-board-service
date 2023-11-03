package org.morriswa.messageboard.stores;

import org.morriswa.messageboard.model.responsebody.CommunityResponse;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;

import java.io.IOException;

public interface CommunityResourceStore {
    void setCommunityBanner(UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    void setCommunityIcon(UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    CommunityResponse.AllCommunityResourceURLs getAllCommunityResources(Long communityId);
}

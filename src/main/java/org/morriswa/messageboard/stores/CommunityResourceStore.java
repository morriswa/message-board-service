package org.morriswa.messageboard.stores;

import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.model.AllCommunityResourceURLs;

import java.io.IOException;

public interface CommunityResourceStore {
    void setCommunityBanner(UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    void setCommunityIcon(UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    AllCommunityResourceURLs getAllCommunityResources(Long communityId);
}

package org.morriswa.messageboard.stores;

import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.model.UploadImageRequest;

import java.io.IOException;

public interface CommunityResourceStore {
    void setCommunityBanner(UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    void setCommunityIcon(UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    Community.AllCommunityResourceURLs getAllCommunityResources(Long communityId);
}

package org.morriswa.messageboard.service.util;

import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.model.AllCommunityResourceURLs;

import java.io.IOException;

public interface ResourceService {
    void setCommunityBanner(UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    void setCommunityIcon(UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    AllCommunityResourceURLs getAllCommunityResources(Long communityId);
}

package org.morriswa.communityservice.service.util;

import org.morriswa.common.model.UploadImageRequest;
import org.morriswa.communityservice.model.AllCommunityResourceURLs;

import java.io.IOException;

public interface ResourceService {
    void setCommunityBanner(UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    void setCommunityIcon(UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    AllCommunityResourceURLs getAllCommunityResources(Long communityId);
}

package org.morriswa.messageboard.store;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.model.UploadImageRequest;

import java.io.IOException;
import java.net.URL;

public interface CommunityResourceStore {
    void setCommunityBanner(@Valid UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    void setCommunityIcon(@Valid UploadImageRequest uploadImageRequest, Long communityId) throws IOException;

    Community.Response.AllCommunityResourceURLs getAllCommunityResources(Long communityId);

    URL getCommunityIcon(Long aLong);
}

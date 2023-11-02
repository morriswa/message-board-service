package org.morriswa.messageboard.stores;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.stores.util.CustomS3ServiceImpl;
import org.morriswa.messageboard.stores.util.ImageScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service @Slf4j
public class CommunityResourceStoreImpl implements CommunityResourceStore {

    private final String COMMUNITY_ICON_PATH;
    private final String COMMUNITY_BANNER_PATH;

    private final ImageScaleService iss;
    private final CustomS3ServiceImpl s3Store;
    @Autowired
    CommunityResourceStoreImpl(Environment e, ImageScaleService iss1, CustomS3ServiceImpl s3Store) {
        this.COMMUNITY_ICON_PATH = e.getRequiredProperty("common.stores.community-icons");
        this.COMMUNITY_BANNER_PATH = e.getRequiredProperty("common.stores.community-banners");
        this.iss = iss1;
        this.s3Store = s3Store;
    }


    @Override
    public void setCommunityBanner(UploadImageRequest uploadImageRequest, Long communityId) throws IOException {
        var fileToUpload = iss.getImageScaledByPercent(uploadImageRequest, 0.6f);

        s3Store.uploadObjectToS3(fileToUpload, this.COMMUNITY_BANNER_PATH+communityId);
    }

    @Override
    public void setCommunityIcon(UploadImageRequest uploadImageRequest, Long communityId) throws IOException {
        var fileToUpload = iss.getImageScaledByPercent(uploadImageRequest, 0.6f);

        s3Store.uploadObjectToS3(fileToUpload, this.COMMUNITY_ICON_PATH+communityId);
    }

    @Override
    public Community.AllCommunityResourceURLs getAllCommunityResources(Long communityId) {
        var response = new Community.AllCommunityResourceURLs();

        if (s3Store.doesObjectExist(COMMUNITY_BANNER_PATH+communityId)) {
            var bannerUrl = s3Store.getSignedObjectUrl(COMMUNITY_BANNER_PATH+communityId, 60);
            response.setBanner(bannerUrl);
        } else response.setBanner(null);

        if (s3Store.doesObjectExist(COMMUNITY_ICON_PATH+communityId)) {
            var bannerUrl = s3Store.getSignedObjectUrl(COMMUNITY_ICON_PATH+communityId, 60);
            response.setIcon(bannerUrl);
        } else response.setIcon(null);

        return response;
    }
}

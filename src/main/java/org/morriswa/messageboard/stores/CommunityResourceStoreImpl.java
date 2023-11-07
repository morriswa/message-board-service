package org.morriswa.messageboard.stores;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.responsebody.CommunityResponse;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.morriswa.messageboard.util.CustomS3UtilImpl;
import org.morriswa.messageboard.util.ImageScaleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component @Slf4j
public class CommunityResourceStoreImpl implements CommunityResourceStore {

    private final float BANNER_SCALE_FACTOR;
    private final int ICON_SIZE;
    private final int SIGNED_URL_EXPIRATION_MINUTES;
    private final String COMMUNITY_ICON_PATH;
    private final String COMMUNITY_BANNER_PATH;
    private final ImageScaleUtil iss;
    private final CustomS3UtilImpl s3Store;

    @Autowired
    CommunityResourceStoreImpl(Environment e, ImageScaleUtil iss1, CustomS3UtilImpl s3Store) {
        this.COMMUNITY_ICON_PATH =
                e.getRequiredProperty("common.stores.prefix")+
                e.getRequiredProperty("common.stores.community-icons");
        this.COMMUNITY_BANNER_PATH =
                e.getRequiredProperty("common.stores.prefix")+
                e.getRequiredProperty("common.stores.community-banners");
        this.SIGNED_URL_EXPIRATION_MINUTES = Integer.parseInt(
                e.getRequiredProperty("common.service.rules.signed-url-expiration-minutes"));
        this.ICON_SIZE = Integer.parseInt(
                e.getRequiredProperty("common.service.rules.square-icon-image-dimension"));
        this.BANNER_SCALE_FACTOR = Float.parseFloat(
                e.getRequiredProperty("community.service.rules.banner-scale-factor"));
        this.iss = iss1;
        this.s3Store = s3Store;
    }

    @Override
    public void setCommunityBanner(@Valid UploadImageRequest uploadImageRequest, Long communityId) throws IOException {
        var image = iss.getScaledImage(uploadImageRequest, BANNER_SCALE_FACTOR);

        s3Store.uploadToS3(image, uploadImageRequest, this.COMMUNITY_BANNER_PATH+communityId);
    }

    @Override
    public void setCommunityIcon(@Valid UploadImageRequest uploadImageRequest, Long communityId) throws IOException {
        var image = iss.getScaledImage(uploadImageRequest, ICON_SIZE, ICON_SIZE);

        s3Store.uploadToS3(image, uploadImageRequest, this.COMMUNITY_ICON_PATH+communityId);
    }

    @Override
    public CommunityResponse.AllCommunityResourceURLs getAllCommunityResources(Long communityId) {
        var response = new CommunityResponse.AllCommunityResourceURLs();

        if (s3Store.doesObjectExist(COMMUNITY_BANNER_PATH+communityId)) {
            var bannerUrl = s3Store.getSignedObjectUrl(COMMUNITY_BANNER_PATH+communityId, SIGNED_URL_EXPIRATION_MINUTES);
            response.setBanner(bannerUrl);
        } else response.setBanner(null);

        if (s3Store.doesObjectExist(COMMUNITY_ICON_PATH+communityId)) {
            var bannerUrl = s3Store.getSignedObjectUrl(COMMUNITY_ICON_PATH+communityId, SIGNED_URL_EXPIRATION_MINUTES);
            response.setIcon(bannerUrl);
        } else response.setIcon(null);

        return response;
    }
}

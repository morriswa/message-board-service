package org.morriswa.messageboard.service.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.AllCommunityResourceURLs;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.validation.CommunityServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service @Slf4j
public class CommunityResourceServiceImpl extends GenericS3Store implements CommunityResourceService {

    private final String COMMUNITY_ICON_PATH;
    private final String COMMUNITY_BANNER_PATH;

    @Autowired
    CommunityResourceServiceImpl(Environment e, ImageScaleService iss) {
        super(e,iss);
        this.COMMUNITY_ICON_PATH = e.getRequiredProperty("community.service.stores.icons");
        this.COMMUNITY_BANNER_PATH = e.getRequiredProperty("community.service.stores.banners");
    }


    @Override
    public void setCommunityBanner(UploadImageRequest uploadImageRequest, Long communityId) throws IOException {
        var fileToUpload = imageScaleService.getImageScaledByPercent(uploadImageRequest, 0.6f);

        uploadObjectToS3(fileToUpload, this.COMMUNITY_BANNER_PATH+communityId);
    }

    @Override
    public void setCommunityIcon(UploadImageRequest uploadImageRequest, Long communityId) throws IOException {
        var fileToUpload = imageScaleService.getImageScaledByPercent(uploadImageRequest, 0.6f);

        uploadObjectToS3(fileToUpload, this.COMMUNITY_ICON_PATH+communityId);
    }

    @Override
    public org.morriswa.messageboard.model.AllCommunityResourceURLs getAllCommunityResources(Long communityId) {
        var response = new AllCommunityResourceURLs();

        Date expiration = new Date();
        long expTimeMillis = Instant.now().toEpochMilli();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        if (doesObjectExist(COMMUNITY_BANNER_PATH+communityId)) {
            var bannerUrl = getSignedObjectUrl(COMMUNITY_BANNER_PATH+communityId, expiration);
            response.setBanner(bannerUrl);
        } else response.setBanner(null);

        if (doesObjectExist(COMMUNITY_ICON_PATH+communityId)) {
            var bannerUrl = getSignedObjectUrl(COMMUNITY_ICON_PATH+communityId, expiration);
            response.setIcon(bannerUrl);
        } else response.setIcon(null);

        return response;
    }
}

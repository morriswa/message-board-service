package org.morriswa.messageboard.service.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.model.AllCommunityResourceURLs;
import org.morriswa.messageboard.service.CommunityServiceValidator;
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
public class ResourceServiceImpl implements ResourceService {
    private final Environment e;
    private final org.morriswa.messageboard.service.CommunityServiceValidator validator;
    private final Base64.Decoder b64decoder;
    private final AmazonS3 s3;
    private final String ACTIVE_BUCKET;
    private final String COMMUNITY_ICON_PATH;
    private final String COMMUNITY_BANNER_PATH;
    private final CannedAccessControlList acl = CannedAccessControlList.AuthenticatedRead;

    @Autowired
    ResourceServiceImpl(Environment e, CommunityServiceValidator validator) {
        this.e = e;
        this.validator = validator;
        this.s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        this.b64decoder = Base64.getDecoder();
        this.ACTIVE_BUCKET = e.getRequiredProperty("aws.s3.bucket");
        this.COMMUNITY_ICON_PATH = e.getRequiredProperty("community.service.stores.icons");
        this.COMMUNITY_BANNER_PATH = e.getRequiredProperty("community.service.stores.banners");
    }

    private void uploadtos3(UploadImageRequest request, Long communityId, String store) throws IOException {
        validator.validateBeanOrThrow(request);

        final String FILESTORE_PATH = e.getRequiredProperty("server.filecache");
        final byte[] imageRepr = b64decoder.decode(request.getBaseEncodedImage());

        BufferedImage retrievedImage = ImageIO.read(new ByteArrayInputStream(imageRepr));
        // TODO add image scaling factor
//        double IMAGE_SCALE_FACTOR = .85;
//        java.awt.Image scaledImage = retrievedImage.getScaledInstance(
//                (int) Math.floor(retrievedImage.getWidth() * IMAGE_SCALE_FACTOR),
//                (int) Math.floor(retrievedImage.getHeight() * IMAGE_SCALE_FACTOR),
//                java.awt.Image.SCALE_SMOOTH);
//        BufferedImage outputImage = new BufferedImage(
//                (int) Math.floor(retrievedImage.getWidth() * IMAGE_SCALE_FACTOR),
//                (int) Math.floor(retrievedImage.getHeight() * IMAGE_SCALE_FACTOR),
//                BufferedImage.TYPE_INT_RGB);
//        outputImage.getGraphics().drawImage(scaledImage, 0, 0, null);

        if ("heic".equalsIgnoreCase(request.getImageFormat())) {
            request.setImageFormat("jpeg");
        }

        final String newImagePath = communityId.toString()+".png";

        File outfile = new File(FILESTORE_PATH + newImagePath);

        ImageIO.write(retrievedImage, "png", outfile);

        if (!outfile.exists()) {
            throw new IOException(
                    String.format(e.getRequiredProperty("user-profile.service.errors.bad-image-format"), request.getImageFormat()));
        }

        s3.putObject(new PutObjectRequest(ACTIVE_BUCKET,
                store+communityId,
                outfile).withCannedAcl(acl));

        if (!outfile.delete()) {
            throw new FileSystemException(
                    e.getRequiredProperty("user-profile.service.errors.unable-to-delete-cached-file"));
        }
    }

    @Override
    public void setCommunityBanner(UploadImageRequest uploadImageRequest, Long communityId) throws IOException {
        uploadtos3(uploadImageRequest, communityId, this.COMMUNITY_BANNER_PATH);
    }

    @Override
    public void setCommunityIcon(UploadImageRequest uploadImageRequest, Long communityId) throws IOException {
        uploadtos3(uploadImageRequest, communityId, this.COMMUNITY_ICON_PATH);
    }

    @Override
    public org.morriswa.messageboard.model.AllCommunityResourceURLs getAllCommunityResources(Long communityId) {
        var response = new AllCommunityResourceURLs();

        Date expiration = new Date();
        long expTimeMillis = Instant.now().toEpochMilli();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        var bannerUrl = s3.generatePresignedUrl(
                new GeneratePresignedUrlRequest(
                        ACTIVE_BUCKET,
                        COMMUNITY_BANNER_PATH+communityId.toString())
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration));
        response.setBanner(bannerUrl);

        var iconUrl = s3.generatePresignedUrl(
                new GeneratePresignedUrlRequest(
                        ACTIVE_BUCKET,
                        COMMUNITY_ICON_PATH+communityId.toString())
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration));
        response.setIcon(iconUrl);

        return response;
    }
}

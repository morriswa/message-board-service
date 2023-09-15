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
import org.morriswa.messageboard.validation.UserProfileServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service @Slf4j
public class ProfileImageServiceImpl implements ProfileImageService {
    private final Environment e;
    private final UserProfileServiceValidator validator;
    private final Base64.Decoder b64decoder;
    private final AmazonS3 s3;
    private final String ACTIVE_BUCKET;
    private final String PROFILE_DIR;
    private final String DEFAULT_PROFILE_IMAGE_OBJECT_ID;

    @Autowired
    ProfileImageServiceImpl(Environment e,
                            UserProfileServiceValidator validator) {
        this.e = e;
        this.validator = validator;
        this.s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        this.b64decoder = Base64.getDecoder();
        this.ACTIVE_BUCKET = e.getRequiredProperty("aws.s3.bucket");
        this.PROFILE_DIR = e.getRequiredProperty("user-profile.service.stores.profile-images");
        this.DEFAULT_PROFILE_IMAGE_OBJECT_ID = e.getRequiredProperty("user-profile.service.stores.default-profile-image");
    }

    @Override
    public void uploadImageToS3(UUID userId, UploadImageRequest request) throws IOException {
        validator.validateBeanOrThrow(request);

        final String FILESTORE_PATH = e.getRequiredProperty("server.filecache");
        final byte[] imageRepr = b64decoder.decode(request.getBaseEncodedImage());

        BufferedImage retrievedImage = ImageIO.read(new ByteArrayInputStream(imageRepr));

        int IMAGE_SCALE_FACTOR = Integer.parseInt(
                e.getRequiredProperty("user-profile.service.rules.user-profile-image-dimension"));
        java.awt.Image scaledImage = retrievedImage.getScaledInstance(
                IMAGE_SCALE_FACTOR,
                IMAGE_SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(
                IMAGE_SCALE_FACTOR,
                IMAGE_SCALE_FACTOR,
                BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(scaledImage, 0, 0, null);

        if ("heic".equalsIgnoreCase(request.getImageFormat())) {
            request.setImageFormat("jpeg");
        }

        final String newImagePath = userId.toString()+".png";

        File outfile = new File(FILESTORE_PATH + newImagePath);

        ImageIO.write(outputImage, "png", outfile);

        if (!outfile.exists()) {
            throw new IOException(
                String.format(e.getRequiredProperty("user-profile.service.errors.bad-image-format"), request.getImageFormat()));
        }

        s3.putObject(new PutObjectRequest(ACTIVE_BUCKET,
            PROFILE_DIR+userId,
            outfile).withCannedAcl(CannedAccessControlList.Private));

        if (!outfile.delete()) {
            throw new FileSystemException(
                e.getRequiredProperty("user-profile.service.errors.unable-to-delete-cached-file"));
        }

    }

    public URL getSignedUserProfileImage(UUID userId) {
        Date expiration = new Date();
        long expTimeMillis = Instant.now().toEpochMilli();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        boolean doesUserProfileExist;
        try {
            doesUserProfileExist = s3.doesObjectExist(this.ACTIVE_BUCKET, PROFILE_DIR+userId.toString());
        } catch (Exception e) {
            doesUserProfileExist = false;
        }

        if (!doesUserProfileExist)
            return s3.generatePresignedUrl(
                new GeneratePresignedUrlRequest(
                    ACTIVE_BUCKET,
                    DEFAULT_PROFILE_IMAGE_OBJECT_ID)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration));

        return s3.generatePresignedUrl(
                new GeneratePresignedUrlRequest(
                        ACTIVE_BUCKET,
                        PROFILE_DIR+userId.toString())
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration));
    }
}


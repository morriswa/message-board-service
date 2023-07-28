package org.morriswa.messageboard.service.util;

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

import javax.imageio.ImageIO;

import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.service.CommunityServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImageResourceServiceImpl implements ImageResourceService {

    private final Environment e;
    private final CommunityServiceValidator validator;
    private final Base64.Decoder b64decoder;
    private final AmazonS3 s3;
    private final String ACTIVE_BUCKET;
    private final String POST_RESOURCE_IMAGE_STORE;
    private final CannedAccessControlList acl = CannedAccessControlList.AuthenticatedRead;

    @Autowired
    public ImageResourceServiceImpl(Environment e, CommunityServiceValidator validator){
        this.e = e;
        this.validator = validator;
        this.s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        this.b64decoder = Base64.getDecoder();
        this.ACTIVE_BUCKET = e.getRequiredProperty("aws.s3.bucket");
        this.POST_RESOURCE_IMAGE_STORE = e.getRequiredProperty("content.service.stores.resources");
    }

    
    private void uploadtos3(UploadImageRequest request, UUID resourceId) throws IOException {
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

        final String newImagePath = resourceId.toString()+".png";

        File outfile = new File(FILESTORE_PATH + newImagePath);

        ImageIO.write(retrievedImage, "png", outfile);

        if (!outfile.exists()) {
            throw new IOException(
                    String.format(e.getRequiredProperty("user-profile.service.errors.bad-image-format"), request.getImageFormat()));
        }

        s3.putObject(new PutObjectRequest(ACTIVE_BUCKET,
                POST_RESOURCE_IMAGE_STORE+resourceId.toString(),
                outfile).withCannedAcl(acl));

        if (!outfile.delete()) {
            throw new FileSystemException(
                    e.getRequiredProperty("user-profile.service.errors.unable-to-delete-cached-file"));
        }
    }

    @Override
    public void uploadImage(UUID resourceID, UploadImageRequest request) throws IOException {
        uploadtos3(request, resourceID);
    }


    @Override
    public URL retrievedImageResource(UUID resourceId) {
        Date expiration = new Date();
        long expTimeMillis = Instant.now().toEpochMilli();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        return s3.generatePresignedUrl(
                new GeneratePresignedUrlRequest(
                        ACTIVE_BUCKET,
                        POST_RESOURCE_IMAGE_STORE+resourceId.toString())
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration));
    }
    
}

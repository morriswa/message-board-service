package org.morriswa.messageboard.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component @Slf4j
public class CustomS3UtilImpl implements CustomS3Util {
    private final String INTERNAL_FILE_CACHE_PATH;
    private final Environment e;
    private final AmazonS3 s3;
    private final String ACTIVE_BUCKET;

    @Autowired
    CustomS3UtilImpl(Environment e) {
        this.e = e;
        this.s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        this.ACTIVE_BUCKET = e.getRequiredProperty("aws.s3.bucket");
        this.INTERNAL_FILE_CACHE_PATH = e.getRequiredProperty("server.filecache");
    }

    public void uploadToS3(BufferedImage toUpload, String destination) throws IOException {

        final UUID newImagePath = UUID.randomUUID();

        File temp = new File(this.INTERNAL_FILE_CACHE_PATH + newImagePath);

        ImageIO.write(toUpload, "png", temp);

        if (!temp.exists()) {
            throw new IOException("Bad");
//                    String.format(e.getRequiredProperty("user-profile.service.errors.bad-image-format"),
//                            imageRequest.getImageFormat()));
        }

        s3.putObject(new PutObjectRequest(ACTIVE_BUCKET,
                destination,
                temp).withCannedAcl(CannedAccessControlList.Private));

        if (!temp.delete()) {
            throw new FileSystemException(
                    e.getRequiredProperty("user-profile.service.errors.unable-to-delete-cached-file"));
        }
    }

    public boolean doesObjectExist(String pathToCheck) {
        boolean objectExists;
        try {
            objectExists = s3.doesObjectExist(this.ACTIVE_BUCKET, pathToCheck);
        } catch (Exception e) {
            objectExists = false;
        }

        return objectExists;
    }

    public URL getSignedObjectUrl(String pathToObject, int expirationMinutes) {

        Date expiration = new Date();
        long expTimeMillis = Instant.now().toEpochMilli();
        expTimeMillis += 1000 * 60 * expirationMinutes;
        expiration.setTime(expTimeMillis);

        return s3.generatePresignedUrl(
                new GeneratePresignedUrlRequest(
                        ACTIVE_BUCKET,
                        pathToObject)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration));
    }
}

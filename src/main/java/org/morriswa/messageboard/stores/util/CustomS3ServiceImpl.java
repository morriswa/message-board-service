package org.morriswa.messageboard.stores.util;

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
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.time.Instant;
import java.util.Date;

@Service @Slf4j
public class CustomS3ServiceImpl implements CustomS3Service {
    private final Environment e;
    private final AmazonS3 s3;
    private final String ACTIVE_BUCKET;

    @Autowired
    CustomS3ServiceImpl(Environment e) {
        this.e = e;
        this.s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        this.ACTIVE_BUCKET = e.getRequiredProperty("aws.s3.bucket");
    }

    public void uploadObjectToS3(File toUpload, String destination) throws IOException {

        s3.putObject(new PutObjectRequest(ACTIVE_BUCKET,
                destination,
                toUpload).withCannedAcl(CannedAccessControlList.Private));

        if (!toUpload.delete()) {
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

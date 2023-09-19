package org.morriswa.messageboard.service.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.util.Date;

@Slf4j
public class GenericS3Store {
    private final AmazonS3 s3;
    private final String ACTIVE_BUCKET;


    protected final Environment e;
    protected final String INTERNAL_FILE_CACHE_PATH;
    protected final ImageScaleService imageScaleService;

    GenericS3Store(Environment e, ImageScaleService imageScaleService) {
        this.e = e;
        this.INTERNAL_FILE_CACHE_PATH = e.getRequiredProperty("server.filecache");
        this.imageScaleService = imageScaleService;
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

    public URL getSignedObjectUrl(String pathToObject, Date expirationTime) {
        return s3.generatePresignedUrl(
                new GeneratePresignedUrlRequest(
                        ACTIVE_BUCKET,
                        pathToObject)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expirationTime));
    }
}

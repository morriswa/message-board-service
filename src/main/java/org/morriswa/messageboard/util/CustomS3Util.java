package org.morriswa.messageboard.util;

import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public interface CustomS3Util {

    void uploadToS3(UploadImageRequest originalRequest, String destination) throws IOException;

    void uploadToS3(BufferedImage scaledImageToUpload, UploadImageRequest originalRequest, String destination) throws IOException;

    boolean doesObjectExist(String pathToCheck);

    URL getSignedObjectUrl(String pathToObject, int expirationMinutes);
}

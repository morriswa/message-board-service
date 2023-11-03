package org.morriswa.messageboard.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public interface CustomS3Util {

    void uploadToS3(BufferedImage toUpload, String destination) throws IOException;

    boolean doesObjectExist(String pathToCheck);

    URL getSignedObjectUrl(String pathToObject, int expirationMinutes);
}

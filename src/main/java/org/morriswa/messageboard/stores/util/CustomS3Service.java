package org.morriswa.messageboard.stores.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public interface CustomS3Service {

    void uploadObjectToS3(File toUpload, String destination) throws IOException;

    boolean doesObjectExist(String pathToCheck);

    URL getSignedObjectUrl(String pathToObject, int expirationMinutes);
}

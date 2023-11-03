package org.morriswa.messageboard.util;

import org.morriswa.messageboard.model.UploadImageRequest;

import java.io.File;
import java.io.IOException;

/**
 * Provides an easy Interface to scale images across services
 */
public interface ImageScaleUtil {

    /**
     *
     *
     * @param imageRequest
     * @param IMAGE_X
     * @param IMAGE_Y
     * @return
     * @throws IOException
     */
    File getScaledImage(UploadImageRequest imageRequest, int IMAGE_X, int IMAGE_Y) throws IOException;
    File getScaledImage(UploadImageRequest imageRequest, float scale) throws IOException;
}

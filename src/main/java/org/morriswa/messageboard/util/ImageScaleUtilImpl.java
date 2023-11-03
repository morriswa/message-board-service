package org.morriswa.messageboard.util;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Component @Slf4j
public class ImageScaleUtilImpl implements ImageScaleUtil {

    private final Environment e;
    private final Base64.Decoder b64decoder;
    protected final String INTERNAL_FILE_CACHE_PATH;


    @Autowired
    public ImageScaleUtilImpl(Environment e) {
        this.e = e;
        this.INTERNAL_FILE_CACHE_PATH = e.getRequiredProperty("server.filecache");
        this.b64decoder = java.util.Base64.getDecoder();
    }

    @Override
    public BufferedImage getScaledImage(UploadImageRequest imageRequest, int IMAGE_X, int IMAGE_Y) throws IOException {

        final byte[] imageRepr = b64decoder.decode(imageRequest.getBaseEncodedImage());

        BufferedImage retrievedImage = ImageIO.read(new ByteArrayInputStream(imageRepr));

        java.awt.Image scaledImage = retrievedImage.getScaledInstance(
                IMAGE_X,
                IMAGE_Y,
                java.awt.Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(
                IMAGE_X,
                IMAGE_Y,
                BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(scaledImage, 0, 0, null);

//        if ("heic".equalsIgnoreCase(imageRequest.getImageFormat())) {
//            imageRequest.setImageFormat("jpeg");
//        }


        return outputImage;
    }

    @Override
    public BufferedImage getScaledImage(UploadImageRequest imageRequest, float scale) throws IOException {

        final byte[] imageRepr = b64decoder.decode(imageRequest.getBaseEncodedImage());

        BufferedImage retrievedImage = ImageIO.read(new ByteArrayInputStream(imageRepr));

        final int IMAGE_X = (int) (retrievedImage.getWidth() * scale);
        final int IMAGE_Y = (int) (retrievedImage.getHeight() * scale);

        java.awt.Image scaledImage = retrievedImage.getScaledInstance(
                IMAGE_X,
                IMAGE_Y,
                java.awt.Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(
                IMAGE_X,
                IMAGE_Y,
                BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(scaledImage, 0, 0, null);

        return outputImage;
    }
}

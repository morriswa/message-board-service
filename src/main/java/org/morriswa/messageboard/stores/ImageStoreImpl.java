package org.morriswa.messageboard.stores;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.morriswa.messageboard.util.CustomS3Util;
import org.morriswa.messageboard.util.ImageScaleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component @Slf4j
public class ImageStoreImpl implements ImageStore {

    private final int SIGNED_URL_EXPIRATION_MINUTES;
    private final float IMAGE_SCALE_FACTOR;
    private final String POST_RESOURCE_IMAGE_STORE;
    private final ImageScaleUtil iss;
    private final CustomS3Util s3Store;

    @Autowired
    public ImageStoreImpl(Environment e,
                          ImageScaleUtil iss,
                          CustomS3Util s3Store){
        this.POST_RESOURCE_IMAGE_STORE =
                e.getRequiredProperty("common.stores.prefix")+
                e.getRequiredProperty("common.stores.post-resources");
        this.IMAGE_SCALE_FACTOR = Float.parseFloat(
                e.getRequiredProperty("content.service.rules.image-scale-factor"));
        this.SIGNED_URL_EXPIRATION_MINUTES = Integer.parseInt(
                e.getRequiredProperty("common.service.rules.signed-url-expiration-minutes"));
        this.iss = iss;
        this.s3Store = s3Store;
    }

    @Override
    public void uploadIndividualImage(UUID resourceID, @Valid UploadImageRequest request) throws IOException {

        if (request.getImageFormat().equals("gif")) {
            s3Store.uploadToS3(request, POST_RESOURCE_IMAGE_STORE+resourceID);
            return;
        }

        var image = iss.getScaledImage(request, IMAGE_SCALE_FACTOR);

        s3Store.uploadToS3(image, request, POST_RESOURCE_IMAGE_STORE+resourceID);
    }

    @Override
    public URL retrieveImageResource(UUID resourceId) {
        return s3Store.getSignedObjectUrl(POST_RESOURCE_IMAGE_STORE+resourceId, SIGNED_URL_EXPIRATION_MINUTES);
    }
}

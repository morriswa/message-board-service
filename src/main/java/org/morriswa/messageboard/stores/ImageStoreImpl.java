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

    private final String POST_RESOURCE_IMAGE_STORE;
    private final ImageScaleUtil iss;
    private final CustomS3Util s3Store;

    @Autowired
    public ImageStoreImpl(Environment e,
                          ImageScaleUtil iss,
                          CustomS3Util s3Store){
        this.POST_RESOURCE_IMAGE_STORE = e.getRequiredProperty("common.stores.post-resources");
        this.iss = iss;
        this.s3Store = s3Store;
    }

    @Override
    public void uploadIndividualImage(UUID resourceID, @Valid UploadImageRequest request) throws IOException {

        var image = iss.getScaledImage(request, 0.8f);

        s3Store.uploadToS3(image, POST_RESOURCE_IMAGE_STORE+resourceID);
    }

    @Override
    public URL retrieveImageResource(UUID resourceId) {
        return s3Store.getSignedObjectUrl(POST_RESOURCE_IMAGE_STORE+resourceId, 60);
    }
}

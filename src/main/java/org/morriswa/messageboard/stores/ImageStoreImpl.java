package org.morriswa.messageboard.stores;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.stores.util.CustomS3Service;
import org.morriswa.messageboard.stores.util.ImageScaleService;
import org.morriswa.messageboard.validation.CommunityServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImageStoreImpl implements ImageStore {

    private final CommunityServiceValidator validator;
    private final String POST_RESOURCE_IMAGE_STORE;
    private final ImageScaleService iss;
    private final CustomS3Service s3Store;

    @Autowired
    public ImageStoreImpl(Environment e,
                          CommunityServiceValidator validator,
                          ImageScaleService iss,
                          CustomS3Service s3Store){
        this.validator = validator;
        this.POST_RESOURCE_IMAGE_STORE = e.getRequiredProperty("common.stores.post-resources");
        this.iss = iss;
        this.s3Store = s3Store;
    }


    @Override
    public void uploadIndividualImage(UUID resourceID, UploadImageRequest request) throws IOException {
        validator.validateBeanOrThrow(request);

        var image = iss.getImageScaledByPercent(request, 0.8f);

        s3Store.uploadObjectToS3(image, POST_RESOURCE_IMAGE_STORE+resourceID);
    }


    @Override
    public URL retrieveImageResource(UUID resourceId) {
        return s3Store.getSignedObjectUrl(POST_RESOURCE_IMAGE_STORE+resourceId, 60);
    }
    
}

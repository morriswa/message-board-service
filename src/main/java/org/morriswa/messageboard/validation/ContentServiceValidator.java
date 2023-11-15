package org.morriswa.messageboard.validation;

import io.micrometer.common.util.StringUtils;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ContentServiceValidator extends BasicBeanValidator {

    private final Environment e;
    public ContentServiceValidator(Environment e) {
        super();
        this.e = e;
    }

    public void validateImageRequestOrThrow(UploadImageRequest uploadRequest) throws ValidationException {
        var errors = new ArrayList<ValidationException.ValidationError>();

        if (StringUtils.isBlank(uploadRequest.getImageFormat()))
            errors.add(new ValidationException.ValidationError(
                    "imageFormat",
                    uploadRequest.getImageFormat(),
                    e.getRequiredProperty("common.service.errors.upload-image-request.empty-image-format")));

//        if (StringUtils.isBlank(uploadRequest.getBaseEncodedImage()))
//            errors.add(new ValidationException.ValidationError(
//                    "baseEncodedImage",
//                    uploadRequest.getBaseEncodedImage().substring(0, 10).concat("..."),
//                    e.getRequiredProperty("common.service.errors.upload-image-request.bad-image-repr")));

        switch (uploadRequest.getImageFormat().toLowerCase()) {
            case "jpg", "jpeg", "png", "gif":
                break;
            default:
                errors.add(new ValidationException.ValidationError(
                    "imageFormat",
                    uploadRequest.getImageFormat(), String.format(
                    e.getRequiredProperty("common.service.errors.upload-image-request.bad-image-format"),
                        uploadRequest.getImageFormat())));
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}

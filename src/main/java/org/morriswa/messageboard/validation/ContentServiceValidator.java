package org.morriswa.messageboard.validation;

import io.micrometer.common.util.StringUtils;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Service
public class ContentServiceValidator extends BasicBeanValidator {
    public ContentServiceValidator() {
        super();
    }

    public void validateImageRequestOrThrow(UploadImageRequest uploadRequest) throws ValidationException {
        var errors = new ArrayList<ValidationException.ValidationError>();

        if (StringUtils.isBlank(uploadRequest.getImageFormat()))
            errors.add(new ValidationException.ValidationError(
                    "imageFormat",
                    uploadRequest.getImageFormat(),
                    "image format must NOT be empty"));

        if (StringUtils.isBlank(uploadRequest.getBaseEncodedImage()))
            errors.add(new ValidationException.ValidationError(
                    "baseEncodedImage",
                    uploadRequest.getBaseEncodedImage().substring(0, 10).concat("..."),
                    "image blob must NOT be empty"));

        switch (uploadRequest.getImageFormat().toLowerCase()) {
            case "jpg", "jpeg", "png", "gif":
                break;
            default:
                errors.add(new ValidationException.ValidationError(
                    "imageFormat",
                    uploadRequest.getImageFormat(),
                    //todo
                    "image format must NOT be " + uploadRequest.getImageFormat() + "! convert this on the front please :)"));
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}

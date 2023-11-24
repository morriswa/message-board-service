package org.morriswa.messageboard.validation;

import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.validatedrequest.CommentRequest;
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

    public void validate(UploadImageRequest uploadRequest) throws ValidationException {
        var errors = new ArrayList<ValidationException.ValidationError>();

        if (uploadRequest.getImageFormat().isBlank())
            errors.add(new ValidationException.ValidationError(
                    "imageFormat",
                    uploadRequest.getImageFormat(),
                    e.getRequiredProperty("common.service.errors.upload-image-request.empty-image-format")));

        if (uploadRequest.getBaseEncodedImage() == null)
            errors.add(new ValidationException.ValidationError(
                    "baseEncodedImage",
                    null,
                    e.getRequiredProperty("common.service.errors.upload-image-request.bad-image-repr")));

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

    public void validate(CommentRequest newComment) throws ValidationException {

        var errors = new ArrayList<ValidationException.ValidationError>();

        final int MAX_LENGTH = Integer.parseInt(
                e.getRequiredProperty("content.service.rules.description.max-length"));

        // defn error messages
        final String ERROR_MSG =
                String.format(e.getRequiredProperty("content.service.errors.bad-comment-length"),
                        MAX_LENGTH);

        final String NULL_ERROR_MSG =
                String.format(e.getRequiredProperty("content.service.errors.null-comment"),
                        MAX_LENGTH);

        if (newComment.getCommentBody().isEmpty()) {
            errors.add(new ValidationException.ValidationError(
                    "body",
                    null,
                    NULL_ERROR_MSG
            ));
        }

        if (newComment.getCommentBody().length() > MAX_LENGTH) {
            errors.add(new ValidationException.ValidationError(
                    "body",
                    newComment.getCommentBody(),
                    ERROR_MSG
            ));
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}

package org.morriswa.messageboard.validation;

import jakarta.validation.*;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.springframework.core.env.Environment;

import java.util.ArrayList;

public abstract class BasicBeanValidator {
    protected final Environment e;
    protected final Validator validator;

    public BasicBeanValidator(Environment e) {
        this.e = e;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /**
     * Uses Jakarta to validate a Bean with constraints
     *
     * @param bean to be validated
     * @throws ConstraintViolationException if Jakarta finds constraint violations
     */
    public void validateBeanOrThrow(@Valid Object bean) {
        var violations = validator.validate(bean);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
    }

    public void validate(UploadImageRequest uploadRequest) throws org.morriswa.messageboard.exception.ValidationException {
        var errors = new ArrayList<org.morriswa.messageboard.exception.ValidationException.ValidationError>();

        if (uploadRequest.imageFormat().isBlank())
            errors.add(new org.morriswa.messageboard.exception.ValidationException.ValidationError(
                    "imageFormat",
                    uploadRequest.imageFormat(),
                    e.getRequiredProperty("common.service.errors.upload-image-request.empty-image-format")));

        if (uploadRequest.baseEncodedImage() == null)
            errors.add(new org.morriswa.messageboard.exception.ValidationException.ValidationError(
                    "baseEncodedImage",
                    null,
                    e.getRequiredProperty("common.service.errors.upload-image-request.bad-image-repr")));

        switch (uploadRequest.imageFormat().toLowerCase()) {
            case "jpg", "jpeg", "png", "gif":
                break;
            default:
                errors.add(new org.morriswa.messageboard.exception.ValidationException.ValidationError(
                        "imageFormat",
                        uploadRequest.imageFormat(), String.format(
                        e.getRequiredProperty("common.service.errors.upload-image-request.bad-image-format"),
                        uploadRequest.imageFormat())));
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    public String missingRequiredField(String field) {
        return String.format(
                e.getRequiredProperty("common.service.errors.missing-required-fields"), field
        );

    }
}

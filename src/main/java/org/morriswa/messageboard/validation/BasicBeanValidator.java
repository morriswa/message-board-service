package org.morriswa.messageboard.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.morriswa.messageboard.enumerated.RequestField;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

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
     */
    public ArrayList<ValidationException.ValidationError> generateConstraintViolations(Object bean) {

        var response = new ArrayList<ValidationException.ValidationError>();

        var violations = validator.validate(bean);

        for (var violation : violations) {
            response.add(new ValidationException.ValidationError(violation.getPropertyPath().toString(),
                    violation.getMessage().contains("null")? RequestField.REQUIRED: RequestField.OPTIONAL,
                    ((String) violation.getInvalidValue()), violation.getMessage()));
        }

        return response;
    }

    public void validate(UploadImageRequest uploadRequest) throws org.morriswa.messageboard.exception.ValidationException {
        var errors = new ArrayList<org.morriswa.messageboard.exception.ValidationException.ValidationError>();

        if (uploadRequest.imageFormat().isBlank())
            errors.add(new org.morriswa.messageboard.exception.ValidationException.ValidationError(
                    "imageFormat",
                    RequestField.REQUIRED,
                    uploadRequest.imageFormat(),
                    e.getRequiredProperty("common.service.errors.upload-image-request.empty-image-format")));

        if (uploadRequest.baseEncodedImage() == null)
            errors.add(new org.morriswa.messageboard.exception.ValidationException.ValidationError(
                    "baseEncodedImage",
                    RequestField.REQUIRED,
                    null,
                    e.getRequiredProperty("common.service.errors.upload-image-request.bad-image-repr")));

        switch (uploadRequest.imageFormat().toLowerCase()) {
            case "jpg", "jpeg", "png", "gif":
                break;
            default:
                errors.add(new org.morriswa.messageboard.exception.ValidationException.ValidationError(
                        "imageFormat",
                        RequestField.REQUIRED,
                        uploadRequest.imageFormat(), String.format(
                        e.getRequiredProperty("common.service.errors.upload-image-request.bad-image-format"),
                        uploadRequest.imageFormat())));
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    public ValidationException.ValidationError missingRequiredField(String field) {
        return new ValidationException.ValidationError(
                field,
                RequestField.REQUIRED,
                null,
                e.getRequiredProperty("common.service.errors.missing-required-field"));
    }

    public List<ValidationException.ValidationError> requiredOneOptionalField(Object... fields) {

        assert fields.length % 2 == 0;

        final String msg = e.getRequiredProperty("common.service.errors.missing-optional-fields");

        boolean allNull = true;

        for (int i = 1; i < fields.length; i+=2) {
            if (fields[i] != null) {
                allNull = false;
                break;
            }
        }

        final var response = new ArrayList<ValidationException.ValidationError>();

        if (allNull) {
            for (int i = 0; i < fields.length; i+=2) {
                response.add(new ValidationException.ValidationError(fields[i].toString(), RequestField.OPTIONAL,null, msg));
            }
        }

        return response;
    }
}

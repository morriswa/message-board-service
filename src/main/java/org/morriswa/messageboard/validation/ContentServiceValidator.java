package org.morriswa.messageboard.validation;

import org.morriswa.messageboard.enumerated.RequestField;
import org.morriswa.messageboard.model.DraftBody;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.CommentRequest;
import org.morriswa.messageboard.model.CreatePostRequest;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContentServiceValidator extends BasicBeanValidator {

    public ContentServiceValidator(Environment e) {
        super(e);
    }

    public void validate(CommentRequest newComment) throws ValidationException {

        var errors = new ArrayList<ValidationException.ValidationError>();

        final int MAX_LENGTH = Integer.parseInt(
                e.getRequiredProperty("content.service.rules.description.max-length"));

        // defn error messages
        final String MAX_LEN_ERROR_MSG =
                String.format(e.getRequiredProperty("content.service.errors.bad-comment-length"),
                        MAX_LENGTH);

        if (newComment.commentBody()==null) {
            errors.add(missingRequiredField("request-body"));
        } else {
            if (newComment.commentBody().length() > MAX_LENGTH) {
                errors.add(new ValidationException.ValidationError(
                        "body",
                        RequestField.REQUIRED,
                        newComment.commentBody(),
                        MAX_LEN_ERROR_MSG
                ));
            }
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }


    private List<ValidationException.ValidationError> getPostCaptionErrors(String caption, RequestField status) {
        final int MIN_LENGTH = Integer.parseInt(
                e.getRequiredProperty("content.service.rules.caption.min-length"));
        final int MAX_LENGTH = Integer.parseInt(
                e.getRequiredProperty("content.service.rules.caption.max-length"));

        var response = new ArrayList<ValidationException.ValidationError>();

        if (status.equals(RequestField.OPTIONAL) && caption == null)
            return response;

        if (caption.length() < MIN_LENGTH || caption.length() > MAX_LENGTH) {
            response.add(new ValidationException.ValidationError("caption",
                    status,
                    caption,
                    String.format(
                            e.getRequiredProperty("content.service.errors.bad-caption-length"),
                            e.getRequiredProperty("content.service.rules.caption.min-length"),
                            e.getRequiredProperty("content.service.rules.caption.max-length")
                    )));
        }

        return response;
    }

    private List<ValidationException.ValidationError> getPostDescriptionErrors(String description, RequestField status) {

        var response = new ArrayList<ValidationException.ValidationError>();

        if (status.equals(RequestField.OPTIONAL) && description == null)
            return response;

        final int MAX_LENGTH = Integer.parseInt(
                e.getRequiredProperty("content.service.rules.description.max-length"));

        if (description.length() > MAX_LENGTH) {
            response.add(new ValidationException.ValidationError("description",status, description,
                    String.format(
                            e.getRequiredProperty("content.service.errors.bad-desc-length"),
                            e.getRequiredProperty("content.service.rules.description.max-length"))));
        }

        return response;
    }

    public void validate(DraftBody draft) throws ValidationException {
        var errors = new ArrayList<ValidationException.ValidationError>();

        errors.addAll(getPostCaptionErrors(draft.caption(), RequestField.OPTIONAL));
        errors.addAll(getPostDescriptionErrors(draft.description(), RequestField.OPTIONAL));

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    public void validateNonNull(DraftBody draft) throws ValidationException {
        var errors = requiredOneOptionalField("caption",draft.caption(), "description", draft.description());
        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    public void validate(CreatePostRequest request) throws ValidationException {

        var errors = new ArrayList<>(generateConstraintViolations(request));

        if (request.caption() != null)
            errors.addAll(getPostCaptionErrors(request.caption(), RequestField.REQUIRED));

        if (request.description() != null)
            errors.addAll(getPostDescriptionErrors(request.description(), RequestField.OPTIONAL));

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}

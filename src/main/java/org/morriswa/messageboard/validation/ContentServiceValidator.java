package org.morriswa.messageboard.validation;

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
        final String ERROR_MSG =
                String.format(e.getRequiredProperty("content.service.errors.bad-comment-length"),
                        MAX_LENGTH);

        final String NULL_ERROR_MSG =
                String.format(e.getRequiredProperty("content.service.errors.null-comment"),
                        MAX_LENGTH);

        if (newComment.commentBody().isEmpty()) {
            errors.add(new ValidationException.ValidationError(
                    "body",
                    null,
                    NULL_ERROR_MSG
            ));
        }

        if (newComment.commentBody().length() > MAX_LENGTH) {
            errors.add(new ValidationException.ValidationError(
                    "body",
                    newComment.commentBody(),
                    ERROR_MSG
            ));
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }


    private List<ValidationException.ValidationError> getPostCaptionErrors(String caption) {
        final int MIN_LENGTH = Integer.parseInt(
                e.getRequiredProperty("content.service.rules.caption.min-length"));
        final int MAX_LENGTH = Integer.parseInt(
                e.getRequiredProperty("content.service.rules.caption.max-length"));


        var response = new ArrayList<ValidationException.ValidationError>();

        if (caption.length() < MIN_LENGTH || caption.length() > MAX_LENGTH) {
            response.add(new ValidationException.ValidationError("caption",caption,
                    String.format(
                            e.getRequiredProperty("content.service.errors.bad-caption-length"),
                            e.getRequiredProperty("content.service.rules.caption.min-length"),
                            e.getRequiredProperty("content.service.rules.caption.max-length")
                    )));
        }

       return response;
    }

    private List<ValidationException.ValidationError> getPostDescriptionErrors(String description) {

        final int MAX_LENGTH = Integer.parseInt(
                e.getRequiredProperty("content.service.rules.description.max-length"));


        var response = new ArrayList<ValidationException.ValidationError>();

        if (description.length() > MAX_LENGTH) {
            response.add(new ValidationException.ValidationError("description",description,
                    String.format(
                            e.getRequiredProperty("content.service.errors.bad-desc-length"),
                            e.getRequiredProperty("content.service.rules.description.max-length"))));
        }

        return response;
    }

    public void validate(DraftBody draft) throws ValidationException {
        var errors = new ArrayList<ValidationException.ValidationError>();

        if (draft.caption() != null) {
            errors.addAll(getPostCaptionErrors(draft.caption()));
        }

        if (draft.description() != null) {
            errors.addAll(getPostDescriptionErrors(draft.description()));
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    public void validateNonNull(DraftBody draft) throws ValidationException {

        if (draft.caption() == null && draft.description() == null)
             throw new ValidationException(null, null, missingRequiredField("[caption||description]"));

        validate(draft);
    }

    public void validate(CreatePostRequest request) throws ValidationException {

        validateBeanOrThrow(request);

        var errors = new ArrayList<>(getPostCaptionErrors(request.caption()));

        if (request.description() != null)
            errors.addAll(getPostDescriptionErrors(request.description()));

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}

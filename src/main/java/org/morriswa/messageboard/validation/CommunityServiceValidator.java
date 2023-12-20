package org.morriswa.messageboard.validation;

import org.morriswa.messageboard.enumerated.RequestField;
import org.morriswa.messageboard.model.UpdateCommunityRequest;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.CreateCommunityRequest;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CommunityServiceValidator extends BasicBeanValidator {

    public CommunityServiceValidator(Environment e) {
        super(e);
    }

    private List<ValidationException.ValidationError> generateLocatorErrors(String ref, RequestField required) {
        // defn rules
        final String communityRefRegexp =
                e.getRequiredProperty("community.service.rules.community-ref.regexp");
        final int MIN_LENGTH = Integer.parseInt(
                e.getRequiredProperty("community.service.rules.community-ref.min-length"));
        final int MAX_LENGTH = Integer.parseInt(
                e.getRequiredProperty("community.service.rules.community-ref.max-length"));

        // defn error messages
        final String ERROR_BAD_COMMUNITY_REF_LENGTH =
                String.format(e.getRequiredProperty("community.service.errors.bad-community-ref-length"),
                MIN_LENGTH, MAX_LENGTH);
        final String ERROR_BAD_COMMUNITY_REF =
                e.getRequiredProperty("community.service.errors.bad-community-ref");

        var errors = new ArrayList<ValidationException.ValidationError>();

        if (MIN_LENGTH>ref.length()||ref.length()>MAX_LENGTH)
            errors.add(new ValidationException.ValidationError(
                    "communityRef",
                    required,
                    ref,
                    ERROR_BAD_COMMUNITY_REF_LENGTH));

        if (!Pattern.matches(communityRefRegexp, ref))
            errors.add(new ValidationException.ValidationError(
                    "communityRef",
                    required,
                    ref,
                    ERROR_BAD_COMMUNITY_REF));

        return errors;
    }

    private List<ValidationException.ValidationError> generateDisplayNameErrors(String displayName, RequestField required) {
        // defn rules
        final int MIN_LENGTH = Integer.parseInt(
                e.getRequiredProperty("community.service.rules.display-name.min-length"));
        final int MAX_LENGTH = Integer.parseInt(
                e.getRequiredProperty("community.service.rules.display-name.max-length"));

        // defn errors
        final String ERROR_BAD_COMMUNITY_DISPLAY_NAME_LENGTH =
                String.format(
                e.getRequiredProperty("community.service.errors.bad-community-display-name-length"),
                MIN_LENGTH, MAX_LENGTH);

        var errors = new ArrayList<ValidationException.ValidationError>();

        if (MIN_LENGTH>displayName.length()||displayName.length()>MAX_LENGTH)
            errors.add(new ValidationException.ValidationError(
                    "communityDisplayName",
                    required,
                    displayName,
                    ERROR_BAD_COMMUNITY_DISPLAY_NAME_LENGTH));

        return errors;
    }

    public void validate(CreateCommunityRequest request) throws ValidationException {
        var errors = new ArrayList<ValidationException.ValidationError>();

        if (request.communityRef() == null) {
            errors.add(missingRequiredField("communityRef"));
        } else {
            var locatorErrors = generateLocatorErrors(request.communityRef(), RequestField.REQUIRED);
            errors.addAll(locatorErrors);
        }

        if (request.communityName() == null) {
            errors.add( missingRequiredField("communityName"));
        } else {
            var communityDisplayNameErrors = generateDisplayNameErrors(request.communityName(), RequestField.REQUIRED);
            errors.addAll(communityDisplayNameErrors);
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    public void validate(UpdateCommunityRequest request) throws ValidationException {
        var errors = new ArrayList<ValidationException.ValidationError>();

        if (request.communityId() == null) {
            errors.add(missingRequiredField("communityId"));
        }

        errors.addAll(requiredOneOptionalField(
                "communityOwnerUserId",request.communityOwnerUserId(),
                "communityLocator",request.communityLocator(),
                "communityDisplayName",request.communityDisplayName()));

        if (request.communityLocator() != null) {
            var locatorErrors = generateLocatorErrors(request.communityLocator(), RequestField.OPTIONAL);
            errors.addAll(locatorErrors);
        }

        if (request.communityDisplayName() != null) {
            var communityDisplayNameErrors = generateDisplayNameErrors(request.communityDisplayName(), RequestField.OPTIONAL);
            errors.addAll(communityDisplayNameErrors);
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}

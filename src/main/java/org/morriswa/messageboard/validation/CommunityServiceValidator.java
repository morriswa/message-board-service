package org.morriswa.messageboard.validation;

import org.morriswa.messageboard.control.requestbody.UpdateCommunityRequest;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.validation.request.CreateCommunityRequest;
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

    private List<ValidationException.ValidationError> generateLocatorErrors(String ref) {
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
                    ref,
                    ERROR_BAD_COMMUNITY_REF_LENGTH));

        if (!Pattern.matches(communityRefRegexp, ref))
            errors.add(new ValidationException.ValidationError(
                    "communityRef",
                    ref,
                    ERROR_BAD_COMMUNITY_REF));

        return errors;
    }

    private List<ValidationException.ValidationError> generateDisplayNameErrors(String displayName) {
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
                    displayName,
                    ERROR_BAD_COMMUNITY_DISPLAY_NAME_LENGTH));

        return errors;
    }

    public void validate(CreateCommunityRequest request) throws ValidationException {
        var errors = new ArrayList<ValidationException.ValidationError>();

        if (request.getCommunityLocator() == null) {
            var error = new ValidationException.ValidationError("communityId", null, "Community ID must not be null!!!");
            errors.add(error);
        } else {
            var locatorErrors = generateLocatorErrors(request.getCommunityLocator());
            errors.addAll(locatorErrors);
        }

        if (request.getCommunityDisplayName() == null) {
            var error = new ValidationException.ValidationError("communityDisplayName", null, "Community Display Name must not be null!!!");
            errors.add(error);
        } else {
            var communityDisplayNameErrors = generateDisplayNameErrors(request.getCommunityDisplayName());
            errors.addAll(communityDisplayNameErrors);
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    public void validate(UpdateCommunityRequest request) throws ValidationException {
        var errors = new ArrayList<ValidationException.ValidationError>();

        if (request.communityId() == null) {
            var error = new ValidationException.ValidationError("communityId", null, "Community ID must not be null!!!");
            errors.add(error);
        }

        if (request.communityLocator() != null) {
            var locatorErrors = generateLocatorErrors(request.communityLocator());
            errors.addAll(locatorErrors);
        }

        if (request.communityDisplayName() != null) {
            var communityDisplayNameErrors = generateDisplayNameErrors(request.communityDisplayName());
            errors.addAll(communityDisplayNameErrors);
        }

        if (request.communityOwnerUserId() == null && request.communityLocator() == null && request.communityDisplayName() == null) {
            var error = new ValidationException.ValidationError(null, null, "Must provide at least one request field to update!");
            errors.add(error);
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}

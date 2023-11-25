package org.morriswa.messageboard.validation;

import org.morriswa.messageboard.exception.ValidationException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Component
public class CommunityServiceValidator extends BasicBeanValidator {

    private final Environment e;
    public CommunityServiceValidator(Environment e) {
        super(e);
        this.e = e;
    }

    public void validateCommunityRefOrThrow(String ref) throws ValidationException {

        // defn rules
        final String communityRefRegexp =
                e.getRequiredProperty("community.service.rules.community-ref.regexp");
        final int MIN_LENGTH = Integer.parseInt(
                e.getRequiredProperty("community.service.rules.community-ref.min-length"));
        final int MAX_LENGTH = Integer.parseInt(
                e.getRequiredProperty("community.service.rules.community-ref.max-length"));

        // defn error messages
        final String ERROR_BAD_COMMUNITY_REF_LENGTH =
                e.getRequiredProperty("community.service.errors.bad-community-ref-length");
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

        if (!errors.isEmpty()) throw new ValidationException(errors);

    }

    public void validateCommunityDisplayNameOrThrow(String ref) throws ValidationException {

        // defn rules
        final int MIN_LENGTH = Integer.parseInt(
                e.getRequiredProperty("community.service.rules.display-name.min-length"));
        final int MAX_LENGTH = Integer.parseInt(
                e.getRequiredProperty("community.service.rules.display-name.max-length"));

        // defn errors
        final String ERROR_BAD_COMMUNITY_DISPLAY_NAME_LENGTH =
                e.getRequiredProperty("community.service.errors.bad-community-display-name-length");

        var errors = new ArrayList<ValidationException.ValidationError>();

        if (MIN_LENGTH>ref.length()||ref.length()>MAX_LENGTH)
            errors.add(new ValidationException.ValidationError(
                    "communityDisplayName",
                    ref,
                    ERROR_BAD_COMMUNITY_DISPLAY_NAME_LENGTH));

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

}

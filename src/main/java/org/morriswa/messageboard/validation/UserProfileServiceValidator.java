package org.morriswa.messageboard.validation;

import org.morriswa.messageboard.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Component
public class UserProfileServiceValidator extends BasicBeanValidator {

    private final Environment e;
    private final String DISPLAY_NAME_REGEXP;
    private final int MIN_LENGTH;
    private final int MAX_LENGTH;

    @Autowired
    public UserProfileServiceValidator(Environment e) {
        super();
        this.e = e;
        DISPLAY_NAME_REGEXP = e.getRequiredProperty("user-profile.service.rules.display-name.regexp");
        MIN_LENGTH = Integer.parseInt(e.getRequiredProperty(
                "user-profile.service.rules.display-name.min-length"));
        MAX_LENGTH = Integer.parseInt(e.getRequiredProperty(
                "user-profile.service.rules.display-name.max-length"));
    }



    public void validateDisplayNameOrThrow(String displayName) throws ValidationException {

        var errors = new ArrayList<ValidationException.ValidationError>();

        if (MIN_LENGTH>displayName.length()||displayName.length()>MAX_LENGTH)
            errors.add(new ValidationException.ValidationError(
                    "displayName",
                    displayName,
                    e.getRequiredProperty("user-profile.service.errors.bad-display-name-length")));

        if (!Pattern.matches(DISPLAY_NAME_REGEXP, displayName))
            errors.add(new ValidationException.ValidationError(
                    "displayName",
                    displayName,
                    e.getRequiredProperty("user-profile.service.errors.bad-display-name")));

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

}

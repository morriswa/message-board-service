package org.morriswa.messageboard.service;

import org.morriswa.messageboard.model.BadRequestException;
import org.morriswa.messageboard.validation.BasicBeanValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserProfileServiceValidator extends BasicBeanValidator {

    private final Environment e;
    private final String DISPLAY_NAME_REGEXP;

    @Autowired
    public UserProfileServiceValidator(Environment e) {
        super();
        this.e = e;
        DISPLAY_NAME_REGEXP = e.getRequiredProperty("user-profile.service.rules.display-name.regexp");
    }

    public void validateDisplayNameOrThrow(String displayName) throws BadRequestException {
        var matches = Pattern.matches(DISPLAY_NAME_REGEXP, displayName);

        if (matches)
            throw new BadRequestException(
                e.getRequiredProperty("user-profile.service.errors.bad-display-name"));
    }

}

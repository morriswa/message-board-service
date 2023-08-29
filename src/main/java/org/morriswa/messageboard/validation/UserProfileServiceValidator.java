package org.morriswa.messageboard.validation;

import com.amazonaws.services.dynamodbv2.xspec.M;
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

    public void validateDisplayNameOrThrow(String displayName) throws BadRequestException {

        if (MIN_LENGTH>displayName.length()||displayName.length()>MAX_LENGTH)
            throw new BadRequestException(
                e.getRequiredProperty("user-profile.service.errors.bad-display-name-length"));

        var matches = Pattern.matches(DISPLAY_NAME_REGEXP, displayName);

        if (!matches)
            throw new BadRequestException(
                e.getRequiredProperty("user-profile.service.errors.bad-display-name"));

    }

}

package org.morriswa.messageboard.validation;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.control.requestbody.NewUserRequestBody;
import org.morriswa.messageboard.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component @Slf4j
public class UserProfileServiceValidator extends BasicBeanValidator {

    private final Environment e;

    @Autowired
    public UserProfileServiceValidator(Environment e) {
        super(e);
        this.e = e;
    }

    public void validateDisplayNameOrThrow(String displayName) throws ValidationException {
        var errors = getErrorsDisplayName(displayName);
        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    private List<ValidationException.ValidationError> getErrorsDisplayName(String displayName) {
        final String DISPLAY_NAME_REGEXP =
                e.getRequiredProperty("user-profile.service.rules.display-name.regexp");
        final int MIN_LENGTH = Integer.parseInt(
                e.getRequiredProperty("user-profile.service.rules.display-name.min-length"));
        final int MAX_LENGTH = Integer.parseInt(
                e.getRequiredProperty("user-profile.service.rules.display-name.max-length"));;

        final String BAD_DISPLAY_NAME_LENGTH_MESSAGE = String.format(
                e.getRequiredProperty("user-profile.service.errors.bad-display-name-length"),
                MIN_LENGTH, MAX_LENGTH
        );

        var errors = new ArrayList<ValidationException.ValidationError>();

        if (MIN_LENGTH>displayName.length()||displayName.length()>MAX_LENGTH)
            errors.add(new ValidationException.ValidationError(
                    "displayName",
                    displayName, BAD_DISPLAY_NAME_LENGTH_MESSAGE
                    ));

        if (!Pattern.matches(DISPLAY_NAME_REGEXP, displayName))
            errors.add(new ValidationException.ValidationError(
                    "displayName",
                    displayName,
                    e.getRequiredProperty("user-profile.service.errors.bad-display-name")));

        return errors;
    }

    public void validate(NewUserRequestBody request) throws ValidationException {
        var errors = new ArrayList<ValidationException.ValidationError>();

        errors.addAll(getErrorsDisplayName(request.displayName()));

        DateTimeFormatter format = DateTimeFormatter.ofPattern(
                e.getRequiredProperty("common.date-format")
        );

        try {
            LocalDate date = LocalDate.parse(request.birthdate(), format);
            if (date.isAfter(LocalDate.parse(e.getRequiredProperty("common.youngest")
                    ,format)))
                errors.add(new ValidationException.ValidationError(
                        "birthdate",
                        request.birthdate(),
                        String.format(e.getRequiredProperty("user-profile.service.errors.too-young"),
                                e.getRequiredProperty("common.youngest"))));
        } catch (DateTimeParseException dtpe) {
            errors.add(new ValidationException.ValidationError(
                    "birthdate",
                    request.birthdate(),
                    dtpe.getMessage()
                    ));
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}

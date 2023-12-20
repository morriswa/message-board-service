package org.morriswa.messageboard.validation;

import org.morriswa.messageboard.enumerated.RequestField;
import org.morriswa.messageboard.model.CreateUserRequest;
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

@Component
public class UserProfileServiceValidator extends BasicBeanValidator {

    private final Environment e;

    @Autowired
    public UserProfileServiceValidator(Environment e) {
        super(e);
        this.e = e;
    }

    public void validateDisplayNameOrThrow(String displayName) throws ValidationException {
        var errors = validateDisplayNameField(displayName, RequestField.REQUIRED);
        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    private List<ValidationException.ValidationError> validateDisplayNameField(String displayName, RequestField status) {

        var errors = new ArrayList<ValidationException.ValidationError>();

        if (status.equals(RequestField.OPTIONAL) && displayName == null)
            return errors;

        final String currentField = "displayName";
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

        if (displayName == null) {
            errors.add(new ValidationException.ValidationError(currentField,status, null,
                    String.format(e.getRequiredProperty("common.service.errors.missing-required-fields"),
                           currentField
                            )));
        } else {

            if (MIN_LENGTH > displayName.length() || displayName.length() > MAX_LENGTH)
                errors.add(new ValidationException.ValidationError(
                        currentField,status,
                        displayName, BAD_DISPLAY_NAME_LENGTH_MESSAGE
                ));

            if (!Pattern.matches(DISPLAY_NAME_REGEXP, displayName))
                errors.add(new ValidationException.ValidationError(
                        currentField,status,
                        displayName,
                        e.getRequiredProperty("user-profile.service.errors.bad-display-name")));
        }

        return errors;
    }

    private List<ValidationException.ValidationError> validateBirthdateField(String birthdate, RequestField status)  {

        var errors = new ArrayList<ValidationException.ValidationError>();

        if (status.equals(RequestField.OPTIONAL) && birthdate == null)
            return errors;

        final String currentField = "birthdate";

        final int MINIMUM_AGE = Integer.parseInt(e.getRequiredProperty("common.minimum-age"));

        if (birthdate== null) {
            errors.add(new ValidationException.ValidationError(currentField, status, null,
                    String.format(e.getRequiredProperty("common.service.errors.missing-required-fields"),
                            currentField
                    )));
        } else {
            DateTimeFormatter format = DateTimeFormatter.ofPattern(
                    e.getRequiredProperty("common.date-format")
            );

            final LocalDate todaysDate = LocalDate.now();
            final LocalDate minimumDate = todaysDate.minusYears(MINIMUM_AGE);

            try {
                LocalDate parsed = LocalDate.parse(birthdate, format);
                if (parsed.isAfter(minimumDate))
                    errors.add(new ValidationException.ValidationError(
                            currentField,
                            status,
                            birthdate,
                            String.format(e.getRequiredProperty("user-profile.service.errors.too-young"),
                                    e.getRequiredProperty("common.minimum-age"))));
            } catch (DateTimeParseException dtpe) {
                errors.add(new ValidationException.ValidationError(
                        currentField,
                        status,
                        birthdate,
                        dtpe.getMessage()
                ));
            }
        }

        return errors;
    }


    public void validate(CreateUserRequest.Body request) throws ValidationException {
        var errors = new ArrayList<ValidationException.ValidationError>();

        errors.addAll(validateDisplayNameField(request.displayName(), RequestField.REQUIRED));
        errors.addAll(validateBirthdateField(request.birthdate(), RequestField.REQUIRED));

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}

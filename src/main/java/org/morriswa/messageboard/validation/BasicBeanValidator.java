package org.morriswa.messageboard.validation;

import jakarta.validation.*;
import org.springframework.stereotype.Component;

@Component
public class BasicBeanValidator {
    protected final Validator validator;

    public BasicBeanValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /**
     * Uses Jakarta to validate a Bean with constraints
     *
     * @param bean to be validated
     * @throws ConstraintViolationException if Jakarta finds constraint violations
     */
    public void validateBeanOrThrow(@Valid Object bean) {
        var violations = validator.validate(bean);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
    }
}

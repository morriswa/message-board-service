package org.morriswa.messageboard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Generic Validator that verifies the Audience of a JWT
 */
@Component
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final String audience;
    private final String errorCode;
    private final String errorDesc;

    @Autowired
    public AudienceValidator(Environment e) {
        this.audience = e.getRequiredProperty("auth0.audience");
        this.errorCode = e.getRequiredProperty("common.service.errors.audience.code");
        this.errorDesc = e.getRequiredProperty("common.service.errors.audience.error");
    }

    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        OAuth2Error error = new OAuth2Error(
                errorCode,
                errorDesc,
                null);

        if (jwt.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(error);
    }
}

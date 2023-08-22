package org.morriswa.messageboard.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Generic Validator that verifies the Audience of a JWT
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final String audience;
    private final String errorCode;
    private final String errorDesc;

    AudienceValidator(String audience, String errorCode, String errorDesc) {
        this.audience = audience;
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
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

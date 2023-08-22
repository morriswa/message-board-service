package org.morriswa.messageboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.morriswa.messageboard.model.DefaultErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import java.util.GregorianCalendar;

/**
 * MorrisWA Custom Spring Security Configurations.
 * Enables Spring Security running as oauth2 resource server.
 *
 * @author William A. Morris [william@morriswa.org]
 */
@Configuration
@EnableWebSecurity // Enables Spring Security for Web Services importing this config
public class WebSecurityConfig
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${server.path}")
    private String path;
    @Value("${common.service.endpoints.health.path}")
    private String healthPath;

    @Value("${auth0.audience}")
    private String audience;
    @Value("${auth0.issuer-uri}")
    private String issuer;
    @Value("${auth0.scope.secureroutes}")
    private String securedRoutesScope;

    @Value("${common.service.errors.security.not-allowed}")
    private String notAllowedError;
    @Value("${common.service.errors.security.not-allowed-desc}")
    private String notAllowedMessage;
    @Value("${common.service.errors.security.invalid-jwt}")
    private String invalidJwtError;
    @Value("${common.service.errors.security.scope-error-message}")
    private String badScopeMessage;
    @Value("${common.service.errors.audience.code}")
    private String audienceErrorCode;
    @Value("${common.service.errors.audience.error}")
    private String audienceErrorDesc;

    @Bean
    protected JwtDecoder jwtDecoder() {
        // all jwts will be decoded with decoder...
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
                JwtDecoders.fromOidcIssuerLocation(issuer); // from User-provided issuer

        // create a new Audience Validator with user-provided audience (should be protected uris)
        OAuth2TokenValidator<Jwt> withAudience = new AudienceValidator(audience, audienceErrorCode, audienceErrorDesc);
        // create a new Jwt Validator from User-provided issuer
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        // create a new Jwt Validator
        OAuth2TokenValidator<Jwt> tokenValidator = new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience);

        // save newly created Validator
        jwtDecoder.setJwtValidator(tokenValidator);

        return jwtDecoder;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http // All http requests will...
                // Be stateless
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

                .authorizeHttpRequests(authorize -> authorize
                        // Will allow any request on /health endpoint
                        .requestMatchers("/"+healthPath).permitAll()
                        // Will require authentication for secured routes
                        .requestMatchers("/" + path + "**").hasAuthority("SCOPE_"+securedRoutesScope)
                        // Will deny all other unauthenticated requests
                        .anyRequest().denyAll())
                // Will allow cors
                .cors().and()

                .exceptionHandling()
                // UNAUTHENTICATED REQUEST ERROR HANDLING
                    .authenticationEntryPoint((request, response, authException) -> {
                        var customErrorResponse = DefaultErrorResponse.builder()
                                .error(notAllowedError)
                                .message(notAllowedMessage)
                                .timestamp(new GregorianCalendar())
                                .build();

                        response.getOutputStream().println(
                                objectMapper.writeValueAsString(customErrorResponse));
                        response.setContentType("application/json");
                        response.setStatus(401);
                    })
                // INSUFFICIENT SCOPE ERROR HANDLING
                    .accessDeniedHandler((request, response, authException) -> {
                        var customErrorResponse = DefaultErrorResponse.builder()
                                .error(notAllowedError)
                                .message(badScopeMessage)
                                .timestamp(new GregorianCalendar())
                                .build();

                        response.getOutputStream().println(
                                objectMapper.writeValueAsString(customErrorResponse));
                        response.setContentType("application/json");
                        response.setStatus(403);
                    })
                .and()

                // Conform toto oauth2 security standards
                .oauth2ResourceServer()
                    .authenticationEntryPoint((request, response, exception) -> {
                        var customErrorResponse = DefaultErrorResponse.builder()
                                .error(invalidJwtError)
                                .message(exception.getMessage())
                                .timestamp(new GregorianCalendar())
                                .build();

                        response.getOutputStream().println(
                                objectMapper.writeValueAsString(customErrorResponse));

                        response.setContentType("application/json");
                        response.setStatus(401);
                    })
                    .accessDeniedHandler((request, response, authException) -> {
                        var customErrorResponse = DefaultErrorResponse.builder()
                                .error(notAllowedError)
                                .message(notAllowedMessage)
                                .timestamp(new GregorianCalendar())
                                .build();

                        response.getOutputStream().println(
                                objectMapper.writeValueAsString(customErrorResponse));
                        response.setContentType("application/json");
                        response.setStatus(403);
                    })
                // provide a jwt
                .jwt();

                

        return http.build();
    }
}
package org.morriswa.messageboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.morriswa.messageboard.model.responsebody.DefaultErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.ArrayList;
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
    private final Environment e;
    private final AudienceValidator audienceValidator;

    @Autowired
    public WebSecurityConfig(Environment e, AudienceValidator audienceValidator) {
        this.e = e;
        this.audienceValidator = audienceValidator;
    }

    @Bean @Profile("!test")
    protected JwtDecoder jwtDecoder() {
        final String issuer = e.getRequiredProperty("auth0.issuer-uri");

        // all jwts will be decoded with decoder from issuer...
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);

        // create a new Jwt Validator
        OAuth2TokenValidator<Jwt> tokenValidator = new DelegatingOAuth2TokenValidator<>(
                // from User-provided issuer
                JwtValidators.createDefaultWithIssuer(issuer),
                // and User-provided audience validator
                audienceValidator);

        // save newly created Validator
        jwtDecoder.setJwtValidator(tokenValidator);

        return jwtDecoder;
    }

    private AuthorizationManager<RequestAuthorizationContext> getScopeAuthManagers() {
        ArrayList<AuthorityAuthorizationManager<Object>> list = new ArrayList<>();
        final String[] securedRoutesScope =
                e.getRequiredProperty("auth0.scope.secureroutes", String[].class);

        for (String scope : securedRoutesScope)
            list.add(AuthorityAuthorizationManager.hasAuthority("SCOPE_"+scope));

        return AuthorizationManagers.allOf(list.toArray(new AuthorityAuthorizationManager[0]));
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {

        final ObjectMapper objectMapper = new ObjectMapper();

        final String path = e.getRequiredProperty("server.path");
        final String healthPath = e.getRequiredProperty("common.service.endpoints.health.path");

        http // All http requests will...
                // Be stateless
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

                .authorizeHttpRequests(authorize -> authorize
                        // Will allow any request on /health endpoint
                        .requestMatchers("/"+healthPath).permitAll()
                        // Will require authentication for secured routes
                        .requestMatchers("/" + path + "**")
//                        .hasAuthority("SCOPE_"+securedRoutesScope)
                        .access(getScopeAuthManagers())
                        // Will deny all other unauthenticated requests
                        .anyRequest().denyAll())
                // Will allow cors
                .cors().and()

                .exceptionHandling()
                // UNAUTHENTICATED REQUEST ERROR HANDLING
                    .authenticationEntryPoint((request, response, authException) -> {
                        var customErrorResponse = DefaultErrorResponse.builder()
                                .error(e.getRequiredProperty("common.service.errors.security.not-allowed"))
                                .message(e.getRequiredProperty("common.service.errors.security.not-allowed-desc"))
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
                                .error(e.getRequiredProperty("common.service.errors.security.not-allowed"))
                                .message(e.getRequiredProperty("common.service.errors.security.scope-error-message"))
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
                                .error(e.getRequiredProperty("common.service.errors.security.invalid-jwt"))
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
                                .error(e.getRequiredProperty("common.service.errors.security.not-allowed"))
                                .message(e.getRequiredProperty("common.service.errors.security.not-allowed-desc"))
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
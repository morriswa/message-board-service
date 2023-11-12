package org.morriswa.messageboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * MorrisWA Custom Spring Security Configurations.
 * Enables Spring Security running as oauth2 resource server.
 *
 * @author William A. Morris [william@morriswa.org]
 */
@Configuration @Slf4j
@EnableWebSecurity // Enables Spring Security for Web Services importing this config
public class WebSecurityConfig
{
    private final Environment e;
    private final HttpResponseFactoryImpl responseFactory;
    private final AudienceValidator audienceValidator;

    @Autowired
    public WebSecurityConfig(Environment e, HttpResponseFactoryImpl responseFactory, AudienceValidator audienceValidator) {
        this.e = e;
        this.responseFactory = responseFactory;
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

    /**
     * converts Permission claims on Auth0 JWTs to Spring Granted Authorities claims.
     * SOURCE:
     *  https://developer.auth0.com/resources/code-samples/api/spring/basic-role-based-access-control#set-up-and-run-the-spring-project
     * @return
     */
    private JwtAuthenticationConverter makePermissionsConverter() {
        final var jwtAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtAuthoritiesConverter.setAuthoritiesClaimName("permissions");
        jwtAuthoritiesConverter.setAuthorityPrefix("AUTH0_");

        final var jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(jwtAuthoritiesConverter);

        return jwtAuthConverter;
    }

    private AuthorizationManager<RequestAuthorizationContext> getConfiguredAuthorizationManager() {
        List<AuthorityAuthorizationManager<Object>> list = new ArrayList<>();
        final String permissionString = e.getProperty("auth0.rbac.permissions", "none");

        if (permissionString.equals("none"))
            return AuthorizationManagers.allOf();

        final List<String> permissions = List.of(permissionString.split(" "));

        for (String scope : permissions)
            list.add(AuthorityAuthorizationManager.hasAuthority("AUTH0_"+scope));

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
                        // Will require authentication and proper permissions for secured routes
                        .requestMatchers("/" + path + "**").access(getConfiguredAuthorizationManager())
                        // Will deny all other unauthenticated requests
                        .anyRequest().denyAll())
                // Will allow cors
                .cors().and()

                .exceptionHandling()
                // UNAUTHENTICATED REQUEST ERROR HANDLING
                    .authenticationEntryPoint((request, response, authException) -> {
                        var customErrorResponse =
                                responseFactory.getErrorResponse(
                                        HttpStatus.UNAUTHORIZED,
                                        e.getRequiredProperty("common.service.errors.security.not-allowed"),
                                        e.getRequiredProperty("common.service.errors.security.not-allowed-desc"));

                        response.getOutputStream().println(
                                objectMapper.writeValueAsString(customErrorResponse.getBody()));
                        response.setContentType("application/json");
                        response.setStatus(customErrorResponse.getStatusCode().value());
                    })
                // INSUFFICIENT SCOPE ERROR HANDLING
                    .accessDeniedHandler((request, response, authException) -> {
                        var customErrorResponse =
                                responseFactory.getErrorResponse(
                                        HttpStatus.FORBIDDEN,
                                        e.getRequiredProperty("common.service.errors.security.not-allowed"),
                                        e.getRequiredProperty("common.service.errors.security.scope-error-message"));

                        response.getOutputStream().println(
                                objectMapper.writeValueAsString(customErrorResponse.getBody()));
                        response.setContentType("application/json");
                        response.setStatus(customErrorResponse.getStatusCode().value());
                    })
                .and()
                // Conform toto oauth2 security standards
                .oauth2ResourceServer()
                    .authenticationEntryPoint((request, response, exception) -> {
                        var customErrorResponse =
                                responseFactory.getErrorResponse(
                                        HttpStatus.UNAUTHORIZED,
                                        e.getRequiredProperty("common.service.errors.security.invalid-jwt"),
                                        exception.getMessage());

                        response.getOutputStream().println(
                                objectMapper.writeValueAsString(customErrorResponse.getBody()));

                        response.setContentType("application/json");
                        response.setStatus(customErrorResponse.getStatusCode().value());
                    })
                    .accessDeniedHandler((request, response, authException) -> {
                        var customErrorResponse = responseFactory.getErrorResponse(
                                HttpStatus.FORBIDDEN,
                                e.getRequiredProperty("common.service.errors.security.not-allowed"),
                                e.getRequiredProperty("common.service.errors.security.not-allowed-desc"));

                        response.getOutputStream().println(
                                objectMapper.writeValueAsString(customErrorResponse.getBody()));
                        response.setContentType("application/json");
                        response.setStatus(customErrorResponse.getStatusCode().value());
                    })
                // provide a jwt
                .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(makePermissionsConverter()));

        return http.build();
    }
}
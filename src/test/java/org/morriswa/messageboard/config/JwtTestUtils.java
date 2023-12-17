package org.morriswa.messageboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service @Profile("test")
public class JwtTestUtils {

    static final String TOKEN = "token";
    static final String SUB = "sms|12345678";

    @Value("${common.secured-permissions}")
    String PERMISSIONS;

    @Value("${testing.email}")
    String TEST_EMAIL;

    public Jwt jwt() {
        // This is a place to add general and maybe custom claims which should be available after parsing token in the live system
        var claims = new HashMap<String, Object>(){{
            put("sub", SUB);
            put("permissions", List.of(PERMISSIONS.split("\\s")));
            put("email", TEST_EMAIL);
        }};

        //This is an object that represents contents of jwt token after parsing
        return new Jwt(
                TOKEN,
                Instant.now(),
                Instant.now().plusSeconds(30),
                Map.of("alg", "none"),
                claims
        );
    }
}

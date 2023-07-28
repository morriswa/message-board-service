package org.morriswa.userprofileservice;

import org.morriswa.common.CommonConfig;
import org.morriswa.common.config.AppConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@Import(CommonConfig.class)
@SpringBootApplication
public class UserProfileServiceApplication {
    public static void main(String[] args) {
        var appConfig = new AppConfig();

        new SpringApplicationBuilder()
            .sources(UserProfileServiceApplication.class)
            .initializers(applicationContext -> {
                try {
                    applicationContext
                            .getEnvironment()
                            .getPropertySources()
                            .addFirst(appConfig.retrieveApplicationPropertySource("user-profile"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .run(args);
    }
}
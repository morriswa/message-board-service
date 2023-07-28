package org.morriswa.contentservice;

import org.morriswa.common.CommonConfig;
import org.morriswa.common.config.AppConfig;
import org.morriswa.communityservice.CommunityServiceRequiredConfig;
import org.morriswa.userprofileservice.UserProfileServiceApplication;
import org.morriswa.userprofileservice.UserProfileServiceRequiredConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@Import({
        CommonConfig.class,
        UserProfileServiceRequiredConfig.class,
        CommunityServiceRequiredConfig.class,
        ContentServiceRequiredConfig.class
})
@SpringBootApplication
public class ContentServiceApplication {

    public static void main(String[] args) {
        var appConfig = new AppConfig();

        new SpringApplicationBuilder()
                .sources(ContentServiceApplication.class)
                .initializers(applicationContext -> {
                    try {
                        applicationContext
                                .getEnvironment()
                                .getPropertySources()
                                .addFirst(appConfig.retrieveApplicationPropertySource("content"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .run(args);
    }

}

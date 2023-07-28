package org.morriswa.communityservice;

import org.morriswa.common.CommonConfig;
import org.morriswa.common.config.AppConfig;
import org.morriswa.userprofileservice.UserProfileServiceRequiredConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@Import({
        CommonConfig.class,
        CommunityServiceRequiredConfig.class,
        UserProfileServiceRequiredConfig.class
})
@SpringBootApplication
public class CommunityServiceRunner {

    public static void main(String[] args) {
        var appConfig = new AppConfig();

        new SpringApplicationBuilder()
                .sources(CommunityServiceRunner.class)
                .initializers(applicationContext -> {
                    try {
                        applicationContext
                                .getEnvironment()
                                .getPropertySources()
                                .addFirst(appConfig.retrieveApplicationPropertySource("community"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .run(args);
    }

}

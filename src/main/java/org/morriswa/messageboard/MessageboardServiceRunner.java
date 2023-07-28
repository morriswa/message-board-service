package org.morriswa.messageboard;

import org.morriswa.messageboard.config.AppConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class MessageboardServiceRunner {

    public static void main(String[] args) {
        var appConfig = new AppConfig();

        new SpringApplicationBuilder()
                .sources(MessageboardServiceRunner.class)
                .initializers(applicationContext -> {
                    try {
                        applicationContext
                                .getEnvironment()
                                .getPropertySources()
                                .addFirst(appConfig.retrieveApplicationPropertySource());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .run(args);
    }

}

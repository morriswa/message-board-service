package org.morriswa.messageboard;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.config.AppConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Objects;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class }) @Slf4j
public class MessageboardServiceRunner {

    public static void main(String[] args) {

        new SpringApplicationBuilder()
                .sources(MessageboardServiceRunner.class)
                .initializers(applicationContext -> {
                    try {
                        final String RUNTIME_ENV = System.getenv("APPCONFIG_ENV_ID");

                        // if the app is not running locally...
                        if (!RUNTIME_ENV.equals("local")) {
                            // bootstrap application config from AWS and inject into current environment
                            applicationContext
                                    .getEnvironment()
                                    .getPropertySources()
                                    .addFirst(AppConfig.build());
                        }

                        // if the app is running in local or local-docker mode...
                        if (RUNTIME_ENV.equals("local") || RUNTIME_ENV.equals("local-docker")) {
                            log.info("DETECTED LOCAL DEVELOPMENT ENVIRONMENT, OVERRIDING DEFAULT USER-CONTENT STORE");
                            System.setProperty("common.stores.prefix",
                                    Objects.requireNonNullElse(
                                            // retrieve manual override of content store from DEV_CONTENT_FOLDER
                                            System.getenv("DEV_CONTENT_FOLDER"),
                                            // if user did not override value, use default-developer content store
                                            "default-developer"));
                        }
                    } catch (Exception e) {
                        // throw any errors encountered while initializing application context as runtime errors
                        throw new RuntimeException(e);
                    }
                })
                .run(args);
    }

}

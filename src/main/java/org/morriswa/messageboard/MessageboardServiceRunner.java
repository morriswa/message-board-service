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
                        switch (System.getenv("APPCONFIG_ENV_ID")) {
                            case "local","local-docker" -> {
                                var userSpecifiedFolder = System.getenv("DEV_CONTENT_FOLDER");
                                System.setProperty("common.stores.prefix",
                                        Objects.requireNonNullElse(userSpecifiedFolder, "default-developer"));
                            }
                            default ->{
                                var appConfig = new AppConfig();

                                applicationContext
                                        .getEnvironment()
                                        .getPropertySources()
                                        .addFirst(appConfig.retrieveApplicationPropertySource());
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .run(args);
    }

}

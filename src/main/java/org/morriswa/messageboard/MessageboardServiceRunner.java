package org.morriswa.messageboard;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.config.AppConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

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

                        // get optional environment variable DEV_CONTENT_FOLDER
                        final String DEV_CONTENT_FOLDER = System.getenv("DEV_CONTENT_FOLDER");
                        // if DEV_CONTENT_FOLDER was set...
                        if (DEV_CONTENT_FOLDER != null) {
                            log.info("OVERRIDING DEFAULT USER-CONTENT STORE WITH {}",DEV_CONTENT_FOLDER);
                            // override "common.stores.prefix" key from AWS_PROPS
                            applicationContext
                                    .getEnvironment()
                                    .getPropertySources()
                                    .addFirst(new PropertiesPropertySource("OVERRIDES", new Properties(){{
                                        put("common.stores.prefix", DEV_CONTENT_FOLDER);
                                    }}));
                        }
                    } catch (Exception e) {
                        // throw any errors encountered while initializing application context as runtime errors
                        throw new RuntimeException(e);
                    }
                })
                .run(args);
    }

}

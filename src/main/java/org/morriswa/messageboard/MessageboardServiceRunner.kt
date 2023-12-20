package org.morriswa.messageboard

import lombok.extern.slf4j.Slf4j
import org.morriswa.messageboard.config.AppConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.PropertiesPropertySource
import java.util.*

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@Slf4j
open class MessageboardServiceRunner {

    companion object {

    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplicationBuilder()
                .sources(MessageboardServiceRunner::class.java)
                .initializers(ApplicationContextInitializer<ConfigurableApplicationContext> { applicationContext: ConfigurableApplicationContext ->
                    // retrieve all possible variables
                    val RUNTIME_ENV = System.getenv("APPCONFIG_ENV_ID")
                    val DEV_CONTENT_FOLDER = System.getenv("DEV_CONTENT_FOLDER")

                    // assert environment variable is not null
                    if (RUNTIME_ENV == null) throw RuntimeException("Environment variable APPCONFIG_ENV_ID must be set! 'local' is a good default...")

                    // if the app is not running locally...
                    if (RUNTIME_ENV != "local") // bootstrap application config from AWS and inject into current environment
                        applicationContext
                                .environment
                                .propertySources
                                .addFirst(AppConfig.build())

                    // if DEV_CONTENT_FOLDER was set...
                    if (DEV_CONTENT_FOLDER != null) {
//                        log.info("OVERRIDING DEFAULT USER-CONTENT STORE WITH {}", DEV_CONTENT_FOLDER)
                        // override "common.stores.prefix" key from AWS_PROPS
                        applicationContext
                                .environment
                                .propertySources
                                .addFirst(PropertiesPropertySource("OVERRIDES", object : Properties() {
                                    init {
                                        put("common.stores.prefix", DEV_CONTENT_FOLDER)
                                    }
                                }))
                    }
                })
                .run(*args)
    }
        }
}

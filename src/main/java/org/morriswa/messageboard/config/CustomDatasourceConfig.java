package org.morriswa.messageboard.config;

import org.morriswa.common.service.util.AmazonSecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class CustomDatasourceConfig {
    @Autowired
    private Environment e;

    @Autowired
    private AmazonSecretService ss;

    @Value("${spring.datasource.auth}")
    private String SPRING_DATASOURCE_AUTH;

    @Bean
    public DataSource getDataSource() {
        return switch (SPRING_DATASOURCE_AUTH) {
            case "false" -> DataSourceBuilder.create()
                    .url(String.format("%s://%s:%s/%s",
                            e.getRequiredProperty("spring.datasource.scheme"),
                            e.getRequiredProperty("spring.datasource.path"),
                            e.getRequiredProperty("spring.datasource.port"),
                            e.getRequiredProperty("spring.datasource.database.name"))).build();
            case "true" -> DataSourceBuilder.create()
                    .username(ss.retrieveKey("database.username"))
                    .password(ss.retrieveKey("database.password"))
                    .url(String.format("%s://%s:%s/%s",
                            e.getRequiredProperty("spring.datasource.scheme"),
                            e.getRequiredProperty("spring.datasource.path"),
                            e.getRequiredProperty("spring.datasource.port"),
                            e.getRequiredProperty("spring.datasource.database.name"))
                    ).build();
            default -> throw new RuntimeException("DATASOURCE COULD NOT BE CONFIGURED, PLEASE CHECK CONFIG <3");
        };
    }
}

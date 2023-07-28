package org.morriswa.messageboard.config;

import org.morriswa.messageboard.service.util.AmazonSecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class CustomDatasourceConfig {
    private final Environment e;
    private final AmazonSecretService ss;
    private final String SPRING_DATASOURCE_AUTH;

    @Autowired
    public CustomDatasourceConfig(Environment e, AmazonSecretService ss) {
        this.e = e;
        this.ss = ss;
        this.SPRING_DATASOURCE_AUTH = e.getRequiredProperty("spring.datasource.auth");
    }

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
            default -> throw new RuntimeException(e.getRequiredProperty("common.service.errors.bad-datasource-config"));
        };
    }
}

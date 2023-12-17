package org.morriswa.messageboard.config;

import com.zaxxer.hikari.HikariDataSource;
import org.morriswa.messageboard.util.AmazonSecretsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("!test")
public class DatasourceConfig {
    private final Environment e;
    private final AmazonSecretsUtil ss;

    @Autowired
    public DatasourceConfig(Environment e, AmazonSecretsUtil ss) {
        this.e = e;
        this.ss = ss;
    }

    @Bean
    public HikariDataSource getDataSource() {
        return switch (e.getRequiredProperty("spring.datasource.auth")) {
            case "false" -> DataSourceBuilder.create().type(HikariDataSource.class)
                    .url(String.format("%s://%s:%s/%s",
                            e.getRequiredProperty("spring.datasource.scheme"),
                            e.getRequiredProperty("spring.datasource.path"),
                            e.getRequiredProperty("spring.datasource.port"),
                            e.getRequiredProperty("spring.datasource.database.name"))).build();
            case "true" -> DataSourceBuilder.create().type(HikariDataSource.class)
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

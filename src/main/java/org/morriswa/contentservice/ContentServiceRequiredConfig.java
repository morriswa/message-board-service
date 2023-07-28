package org.morriswa.contentservice;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan("org.morriswa.contentservice.service")
@EnableJpaRepositories("org.morriswa.contentservice.repo")
@EntityScan("org.morriswa.contentservice.entity")
public class ContentServiceRequiredConfig {
}

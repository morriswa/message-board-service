package org.morriswa.communityservice;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Required Import if you wish to use the User Profile Service with other services (i.e. Content Service)
 */
@ComponentScan("org.morriswa.communityservice.service")
@EnableJpaRepositories("org.morriswa.communityservice.repo")
@EntityScan("org.morriswa.communityservice.entity")
public class CommunityServiceRequiredConfig { }

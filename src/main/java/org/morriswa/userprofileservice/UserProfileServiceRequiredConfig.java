package org.morriswa.userprofileservice;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Required Import if you wish to use the User Profile Service with other services (i.e. Content Service)
 */
@ComponentScan("org.morriswa.userprofileservice.service")
@EnableJpaRepositories("org.morriswa.userprofileservice.repo")
@EntityScan("org.morriswa.userprofileservice.entity")
public class UserProfileServiceRequiredConfig { }

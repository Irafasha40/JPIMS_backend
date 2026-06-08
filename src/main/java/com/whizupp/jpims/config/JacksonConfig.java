package com.whizupp.jpims.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * Serialize Hibernate proxies safely (e.g. lazy associations) instead of failing with
     * ByteBuddyInterceptor / empty bean errors.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonHibernate6Module() {
        return builder -> builder.modulesToInstall(new Hibernate6Module());
    }
}

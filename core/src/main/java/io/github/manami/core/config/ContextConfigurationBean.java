package io.github.manami.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.google.common.eventbus.EventBus;

/**
 * Configuration bean for the spring context.
 *
 * @author manami project
 * @since 2.0.0
 */
@Configuration
@ComponentScan("io.github.manami")
public class ContextConfigurationBean {

    private EventBus eventBus;


    @Bean
    public EventBus eventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }
        return eventBus;
    }
}

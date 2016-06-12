package io.github.manami;

import com.google.common.eventbus.EventBus;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.github.manami.core.config.ContextConfigurationBean;
import io.github.manami.dto.events.ApplicationContextStartedEvent;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point of the application.
 *
 * @author manami-project
 * @since 2.0.0
 */
public class Main extends Application {

    /**
     * Spring context.
     */
    public static final ConfigurableApplicationContext CONTEXT = new AnnotationConfigApplicationContext(ContextConfigurationBean.class);


    /**
     * @param args Command line arguments.
     * @since 2.0.0
     */
    public static void main(final String[] args) {
        launch(args);
    }


    @Override
    public void start(final Stage stage) throws Exception {
        CONTEXT.getBean(EventBus.class).post(new ApplicationContextStartedEvent(stage));
    }
}

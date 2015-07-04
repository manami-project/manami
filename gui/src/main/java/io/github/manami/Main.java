package io.github.manami;

import javafx.application.Application;
import javafx.stage.Stage;
import io.github.manami.core.config.ContextConfigurationBean;
import io.github.manami.dto.events.ApplicationContextStartedEvent;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.eventbus.EventBus;

/**
 * Entry point of the application.
 *
 * @author manami project
 * @since 2.0.0
 */
public class Main extends Application {

    /** Spring context. */
    public static final ConfigurableApplicationContext CONTEXT = new AnnotationConfigApplicationContext(ContextConfigurationBean.class);


    /**
     * @since 2.0.0
     * @param args
     *            Command line arguments.
     */
    public static void main(final String[] args) {
        launch(args);
    }


    @Override
    public void start(final Stage stage) throws Exception {
        CONTEXT.getBean(EventBus.class).post(new ApplicationContextStartedEvent(stage));
    }
}

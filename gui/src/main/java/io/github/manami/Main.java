package io.github.manami;

import java.io.IOException;
import java.nio.file.Paths;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.eventbus.EventBus;

import io.github.manami.core.config.ConfigFileWatchdog;
import io.github.manami.core.config.ContextConfigurationBean;
import io.github.manami.dto.events.ApplicationContextStartedEvent;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point of the application.
 */
public class Main extends Application {

    /**
     * Spring context.
     */
    public static final ConfigurableApplicationContext CONTEXT = new AnnotationConfigApplicationContext(ContextConfigurationBean.class);


    /**
     * @param args Command line arguments.
     */
    public static void main(final String[] args) throws IOException {
        new ConfigFileWatchdog(Paths.get(".")).validate();
        launch(args);
    }


    @Override
    public void start(final Stage stage) throws Exception {
        CONTEXT.getBean(EventBus.class).post(new ApplicationContextStartedEvent(stage));
    }
}

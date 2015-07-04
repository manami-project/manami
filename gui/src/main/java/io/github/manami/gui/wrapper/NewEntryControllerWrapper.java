package io.github.manami.gui.wrapper;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import io.github.manami.gui.DialogLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

/**
 * @author manami project
 * @since 2.7.2
 */
@Named
public class NewEntryControllerWrapper {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(NewEntryControllerWrapper.class);


    /**
     * @since 2.7.2
     */
    public void showNewEntryStage() {
        final Stage newEntryStage = new Stage(StageStyle.UTILITY);
        newEntryStage.setResizable(false);
        newEntryStage.initModality(Modality.APPLICATION_MODAL);
        newEntryStage.centerOnScreen();
        newEntryStage.initStyle(StageStyle.UTILITY);
        newEntryStage.setTitle(MainControllerWrapper.APPNAME + " - Add new entry");

        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("io/github/manami/gui/controller/new_entry.fxml"));
            final Pane pane = fxmlLoader.load();
            newEntryStage.setScene(new Scene(pane));
            newEntryStage.sizeToScene();
        } catch (final Exception e) {
            LOG.error("An error occurred while trying to initialize new entry controller: ", e);
            DialogLibrary.showExceptionDialog(e);
        }

        newEntryStage.show();
    }
}

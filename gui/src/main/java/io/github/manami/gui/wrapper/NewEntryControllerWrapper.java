package io.github.manami.gui.wrapper;

import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;
import static io.github.manami.gui.wrapper.MainControllerWrapper.APPNAME;

import javax.inject.Named;

import org.springframework.core.io.ClassPathResource;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

/**
 * @author manami-project
 * @since 2.7.2
 */
@Named
@Slf4j
public class NewEntryControllerWrapper {

	/**
	 * @since 2.7.2
	 */
	public void showNewEntryStage() {
		final Stage newEntryStage = new Stage(StageStyle.UTILITY);
		newEntryStage.setResizable(false);
		newEntryStage.initModality(Modality.APPLICATION_MODAL);
		newEntryStage.centerOnScreen();
		newEntryStage.initStyle(StageStyle.UTILITY);
		newEntryStage.setTitle(APPNAME + " - Add new entry");

		try {
			final FXMLLoader fxmlLoader = new FXMLLoader(new ClassPathResource("io/github/manami/gui/controller/new_entry.fxml").getURL());
			final Pane pane = fxmlLoader.load();
			newEntryStage.setScene(new Scene(pane));
			newEntryStage.sizeToScene();
		} catch (final Exception e) {
			log.error("An error occurred while trying to initialize new entry controller: ", e);
			showExceptionDialog(e);
		}

		newEntryStage.show();
	}
}

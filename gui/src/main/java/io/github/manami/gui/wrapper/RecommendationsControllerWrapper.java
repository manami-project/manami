package io.github.manami.gui.wrapper;

import static io.github.manami.gui.controller.RecommendationsController.RECOMMENDATIONS_TAB_TITLE;
import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;

import javax.inject.Named;

import org.springframework.core.io.ClassPathResource;

import com.google.common.eventbus.Subscribe;

import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.controller.RecommendationsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

/**
 * @author manami-project
 * @since 2.7.2
 */
@Named
@Slf4j
public class RecommendationsControllerWrapper {

	private Tab recommendationsTab;
	private RecommendationsController recommendationsController;


	/**
	 * @since 2.7.2
	 */
	private void init() {
		recommendationsTab = new Tab(RECOMMENDATIONS_TAB_TITLE);
		Parent pane;
		try {
			final FXMLLoader fxmlLoader = new FXMLLoader(new ClassPathResource("io/github/manami/gui/controller/recommendations_tab.fxml").getURL());
			pane = (Pane) fxmlLoader.load();
			recommendationsTab.setContent(pane);
			recommendationsController = fxmlLoader.getController();
			recommendationsController.setTab(recommendationsTab);
		} catch (final Exception e) {
			log.error("An error occurred while trying to initialize recommendations tab: ", e);
			showExceptionDialog(e);
		}
	}


	/**
	 * @since 2.7.2
	 * @return the recommendationsTab
	 */
	public Tab getRecommendationsTab() {
		if (recommendationsTab == null) {
			init();
		}

		return recommendationsTab;
	}


	/**
	 * @since 2.8.2
	 * @param event
	 *            Event which is fired when a file is opened.
	 */
	@Subscribe
	public void changeEvent(final OpenedFileChangedEvent event) {
		if (recommendationsController == null) {
			init();
		}

		recommendationsController.clear();
	}
}

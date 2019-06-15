package io.github.manami.gui.wrapper;

import static io.github.manami.gui.controller.WatchListController.WATCH_LIST_TITLE;
import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;

import javax.inject.Named;

import org.springframework.core.io.ClassPathResource;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import io.github.manami.dto.events.AnimeListChangedEvent;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.controller.WatchListController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

/**
 * @author manami-project
 * @since 2.8.0
 */
@Named
@Slf4j
public class WatchListControllerWrapper {

	private Tab watchListTab;
	private WatchListController watchListController;


	/**
	 * @since 2.7.2
	 */
	private void init() {
		watchListTab = new Tab(WATCH_LIST_TITLE);

		Parent pane;
		try {
			final FXMLLoader fxmlLoader = new FXMLLoader(new ClassPathResource("io/github/manami/gui/controller/watch_list_tab.fxml").getURL());
			pane = (Pane) fxmlLoader.load();
			watchListController = fxmlLoader.getController();
			watchListController.setTab(watchListTab);
			watchListTab.setContent(pane);
		} catch (final Exception e) {
			log.error("An error occurred while trying to initialize watch list tab: ", e);
			showExceptionDialog(e);
		}
	}


	@Subscribe
	public void changeEvent(final OpenedFileChangedEvent event) {
		if (watchListController == null) {
			init();
		}

		watchListController.clear();
	}


	@Subscribe
	@AllowConcurrentEvents
	public void changeEvent(final AnimeListChangedEvent event) {
		if (watchListController == null) {
			init();
		}
	}


	/**
	 * @since 2.7.2
	 * @return the filterTab
	 */
	public Tab getWatchListTab() {
		if (watchListTab == null) {
			init();
		}

		return watchListTab;
	}
}

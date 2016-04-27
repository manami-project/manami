package io.github.manami.gui.wrapper;

import static io.github.manami.gui.controller.CheckListController.CHECK_LIST_TAB_TITLE;
import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;

import javax.inject.Named;

import org.springframework.core.io.ClassPathResource;

import com.google.common.eventbus.Subscribe;

import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.controller.CheckListController;
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
public class CheckListControllerWrapper {

	private Tab checkListTab;
	private CheckListController checkListController;


	/**
	 * @since 2.7.2
	 */
	private void init() {
		checkListTab = new Tab(CHECK_LIST_TAB_TITLE);
		Parent pane;
		try {
			final FXMLLoader fxmlLoader = new FXMLLoader(new ClassPathResource("io/github/manami/gui/controller/check_list_tab.fxml").getURL());
			pane = (Pane) fxmlLoader.load();
			checkListTab.setContent(pane);
			checkListController = fxmlLoader.getController();
			checkListController.setTab(checkListTab);
		} catch (final Exception e) {
			log.error("An error occurred while trying to initialize check list tab: ", e);
			showExceptionDialog(e);
		}
	}


	/**
	 * @since 2.7.2
	 * @return the checkListTab
	 */
	public Tab getCheckListTab() {
		if (checkListTab == null) {
			init();
		}

		return checkListTab;
	}


	/**
	 * @since 2.8.2
	 * @param event
	 *            Event which is fired when a file is opened.
	 */
	@Subscribe
	public void changeEvent(final OpenedFileChangedEvent event) {
		if (checkListController == null) {
			init();
		}

		checkListController.clear();
	}
}

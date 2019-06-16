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


@Named
@Slf4j
public class CheckListControllerWrapper {

	private Tab checkListTab;
	private CheckListController checkListController;


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


	public Tab getCheckListTab() {
		if (checkListTab == null) {
			init();
		}

		return checkListTab;
	}


	@Subscribe
	public void changeEvent(final OpenedFileChangedEvent event) {
		if (checkListController == null) {
			init();
		}

		checkListController.clear();
	}
}

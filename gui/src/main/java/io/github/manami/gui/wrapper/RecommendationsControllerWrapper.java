package io.github.manami.gui.wrapper;

import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.DialogLibrary;
import io.github.manami.gui.controller.RecommendationsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

/**
 * @author manami-project
 * @since 2.7.2
 */
@Named
public class RecommendationsControllerWrapper {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationsControllerWrapper.class);

    private Tab recommendationsTab;
    private RecommendationsController recommendationsController;


    /**
     * @since 2.7.2
     */
    private void init() {
        recommendationsTab = new Tab(RecommendationsController.RECOMMENDATIONS_TAB_TITLE);
        Parent pane;
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("io/github/manami/gui/controller/recommendations_tab.fxml"));
            pane = (Pane) fxmlLoader.load();
            recommendationsTab.setContent(pane);
            recommendationsController = fxmlLoader.getController();
            recommendationsController.setTab(recommendationsTab);
        } catch (final Exception e) {
            LOG.error("An error occurred while trying to initialize recommendations tab: ", e);
            DialogLibrary.showExceptionDialog(e);
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
     * @param event Event which is fired when a file is opened.
     */
    @Subscribe
    public void changeEvent(final OpenedFileChangedEvent event) {
        if (recommendationsController == null) {
            init();
        }

        recommendationsController.clear();
    }
}

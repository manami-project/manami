package io.github.manami.gui.controller;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.core.services.TagRetrievalService;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.gui.components.AnimeGuiComponentsListEntry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * @author manami-project
 * @since 2.8.0
 */
public class TagListController extends AbstractAnimeListController implements Observer {

    public static final String TAG_LIST_TITLE = "Tags";

    /** Instance of the application. */
    private final Manami app = Main.CONTEXT.getBean(Manami.class);

    /** Instance of the cache. */
    private final Cache cache = Main.CONTEXT.getBean(Cache.class);

    /** Instance of the service repository. */
    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);

    /** {@link TextField} for adding a new entry. */
    @FXML
    private TextField txtUrl;

    /** {@link GridPane} which shows the results. */
    @FXML
    private GridPane gridPane;

    /** Moving circle indicating a process. */
    @FXML
    private ProgressIndicator progressIndicator;

    /** Showing the amount of services running in the background. */
    @FXML
    private Label lblProgressMsg;

    private Tab tab;

    private TagRetrievalService service;


    @Override
    protected GridPane getGridPane() {
        return gridPane;
    }


    @FXML
    public void addEntry() {
        clear();
        final List<String> urlList = Arrays.asList(txtUrl.getText().trim().split(" "));
        final List<InfoLink> infoLinkList = newArrayList();
        urlList.forEach(url -> infoLinkList.add(new InfoLink(url)));
        service = new TagRetrievalService(cache, app, infoLinkList, this);
        serviceRepo.startService(service);
        txtUrl.setText(EMPTY);
        Platform.runLater(() -> {
            progressIndicator.setVisible(true);
            lblProgressMsg.setVisible(true);
            getGridPane().getChildren().clear();
        });
    }


    /**
     * @since 2.8.0
     */
    public void clear() {
        if (service != null) {
            service.deleteObserver(this);
            service.reset();
        }
        Platform.runLater(() -> {
            progressIndicator.setVisible(false);
            lblProgressMsg.setVisible(false);
            getGridPane().getChildren().clear();
        });
        clearComponentList();
        showEntries();
    }


    public void setTab(final Tab tab) {
        this.tab = tab;
    }


    @Override
    protected List<AnimeGuiComponentsListEntry> sortComponentEntries() {
        return newArrayList(getComponentList().values());
    }


    @Override
    protected void updateChildren() {
        Platform.runLater(() -> tab.setText(String.format("%s (%s)", TAG_LIST_TITLE, getComponentList().size())));
    }


    @Override
    protected List<? extends MinimalEntry> getEntryList() {
        // not needed for this controller
        return null;
    }


    @Override
    boolean isInList(final InfoLink infoLink) {
        // not needed for this controller
        return false;
    }


    @Override
    public void update(final Observable observable, final Object object) {
        if (observable == null || object == null) {
            return;
        }

        if (observable instanceof TagRetrievalService && object instanceof Anime) {
            final Anime anime = (Anime) object;
            addEntryToGui(anime); // create GUI components
            showEntries();
        }

        if (observable instanceof TagRetrievalService && object instanceof Boolean) {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                lblProgressMsg.setVisible(false);
            });
        }
    }
}

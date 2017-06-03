package io.github.manami.gui.controller;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

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
import javafx.scene.control.Button;
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

    @FXML
    private Button btnCancel;

    private Tab tab;

    private TagRetrievalService service;


    @Override
    protected GridPane getGridPane() {
        return gridPane;
    }


    @FXML
    public void addEntry() {
        final String urlString = txtUrl.getText().trim();

        if (!isValid(urlString)) {
            return;
        }

        clear();
        service = new TagRetrievalService(cache, app, urlString, this);
        serviceRepo.startService(service);
        txtUrl.setText(EMPTY);
        Platform.runLater(() -> {
            progressIndicator.setVisible(true);
            lblProgressMsg.setVisible(true);
            btnCancel.setVisible(true);
            getGridPane().getChildren().clear();
        });
    }


    private boolean isValid(final String urlString) {
        if (urlString.startsWith("https://myanimelist.net/anime/genre")) {
            return true;
        }

        if (urlString.startsWith("https://myanimelist.net/anime/producer")) {
            return true;
        }

        if (urlString.startsWith("https://myanimelist.net/anime/season")) {
            return true;
        }

        return false;
    }


    /**
     * @since 2.8.0
     */
    public void clear() {
        cancel();

        Platform.runLater(() -> {
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
                btnCancel.setVisible(false);
            });
        }
    }


    public void cancel() {
        if (service != null) {
            service.deleteObserver(this);
            service.reset();
        }

        Platform.runLater(() -> {
            progressIndicator.setVisible(false);
            lblProgressMsg.setVisible(false);
            btnCancel.setVisible(false);
        });
    }
}

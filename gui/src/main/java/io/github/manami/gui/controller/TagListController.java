package io.github.manami.gui.controller;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.core.services.TagRetrievalService;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.gui.utility.AnimeTableBuilder;
import io.github.manami.gui.utility.ImageCache;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import static org.apache.commons.lang3.StringUtils.EMPTY;


public class TagListController implements Observer {

    public static final String TAG_LIST_TITLE = "Tags";

    private final Manami app = Main.CONTEXT.getBean(Manami.class);
    private final Cache cache = Main.CONTEXT.getBean(Cache.class);
    private final ImageCache imageCache = Main.CONTEXT.getBean(ImageCache.class);
    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);
    private final Set<InfoLink> containedEntries = new HashSet<>();

    @FXML
    private TextField txtUrl;

    @FXML
    private HBox hBoxProgress;

    @FXML
    private TableView<Anime> contentTable;

    private Tab tab;
    private TagRetrievalService service;


    public void initialize() {
        new AnimeTableBuilder<>(contentTable)
                .withPicture(imageCache::loadPicture)
                .withTitleSortable(true)
                .withAddToWatchListButton(true)
                .withAddToFilterListButton(true)
                .withRemoveButton(true)
                .withListChangedEvent((a) -> {
                    Platform.runLater(() -> tab.setText(String.format("%s (%s)", TAG_LIST_TITLE, contentTable.getItems().size())));
                    return null;
                });
    }

    @FXML
    public void search() {
        final String urlString = txtUrl.getText().trim();

        if (!isValid(urlString)) {
            return;
        }

        clear();

        service = new TagRetrievalService(cache, app, urlString, this);
        serviceRepo.startService(service);

        Platform.runLater(() -> {
            txtUrl.setText(EMPTY);
            hBoxProgress.setVisible(true);
        });
    }


    private boolean isValid(final String urlString) {
        if (urlString.startsWith("https://myanimelist.net/anime/genre")) {
            return true;
        }

        if (urlString.startsWith("https://myanimelist.net/anime/producer")) {
            return true;
        }

        return urlString.startsWith("https://myanimelist.net/anime/season");
    }


    public void clear() {
        cancel();
        contentTable.getItems().clear();
        containedEntries.clear();
    }


    public void setTab(final Tab tab) {
        this.tab = tab;
    }


    @Override
    public void update(final Observable observable, final Object object) {
        if (observable == null || object == null) {
            return;
        }

        if (observable instanceof TagRetrievalService && object instanceof Anime) {
            Anime anime = (Anime) object;

            if (!containedEntries.contains(anime.getInfoLink())) {
                containedEntries.add(anime.getInfoLink());
                contentTable.getItems().add(anime);
            }
        }

        if (observable instanceof TagRetrievalService && object instanceof Boolean) {
            Platform.runLater(() -> hBoxProgress.setVisible(false));
        }
    }


    public void cancel() {
        if (service != null) {
            service.deleteObserver(this);
            service.reset();
        }

        Platform.runLater(() -> {
            hBoxProgress.setVisible(false);
        });
    }
}

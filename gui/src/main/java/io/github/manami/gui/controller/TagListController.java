package io.github.manami.gui.controller;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdAddFilterEntry;
import io.github.manami.core.commands.CmdAddWatchListEntry;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.core.services.TagRetrievalService;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.gui.utility.HyperlinkBuilder;
import io.github.manami.gui.utility.ReadOnlyObservableValue;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import static io.github.manami.gui.components.Icons.createIconFilterList;
import static io.github.manami.gui.components.Icons.createIconRemove;
import static io.github.manami.gui.components.Icons.createIconWatchList;
import static org.apache.commons.lang3.StringUtils.EMPTY;


public class TagListController implements Observer {

    public static final String TAG_LIST_TITLE = "Tags";

    private Tab tab;
    private TagRetrievalService service;
    private Map<InfoLink, SoftReference<Image>> imageCache = new HashMap<>();

    private final Manami app = Main.CONTEXT.getBean(Manami.class);
    private final Cache cache = Main.CONTEXT.getBean(Cache.class);
    private final CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);
    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);

    @FXML
    private TextField txtUrl;

    @FXML
    private HBox hBoxProgress;

    @FXML
    private TableView<Anime> contentTable;

    @FXML
    private TableColumn<Anime, ImageView> colImage;

    @FXML
    private TableColumn<Anime, Hyperlink> colTitle;

    @FXML
    private TableColumn<Anime, HBox> colActions;


    public void initialize() {
        colImage.setCellValueFactory(p -> new ReadOnlyObservableValue<ImageView>() {

            @Override
            public ImageView getValue() {
                ImageView cachedImageView = new ImageView(loadImage(p.getValue()));
                cachedImageView.setCache(true);
                return cachedImageView;
            }
        });

        colTitle.setCellValueFactory(p -> new ReadOnlyObservableValue<Hyperlink>() {

            @Override
            public Hyperlink getValue() {
                Hyperlink title = HyperlinkBuilder.buildFrom(p.getValue().getTitle(), p.getValue().getInfoLink().toString());
                title.setFont(Font.font(24.0));
                return title;
            }
        });

        colTitle.setComparator((o1, o2) -> o1.getText().compareToIgnoreCase(o2.getText()));

        colActions.setCellValueFactory(p -> new ReadOnlyObservableValue<HBox>() {

            @Override
            public HBox getValue() {
                return createActionButtons(p.getValue());
            }
        });
    }

    private Image loadImage(Anime anime) {
        if (imageCache.containsKey(anime.getInfoLink())) {
            return imageCache.get(anime.getInfoLink()).get();
        }

        Image image = new Image(anime.getPicture(), true);
        imageCache.put(anime.getInfoLink(), new SoftReference<>(image));

        return image;
    }

    private HBox createActionButtons(Anime anime) {
        final Button btnAddToWatchlist = new Button(EMPTY, createIconWatchList());
        btnAddToWatchlist.setTooltip(new Tooltip("add to watch list"));
        btnAddToWatchlist.setOnAction(event -> {
            WatchListEntry.valueOf(anime).ifPresent(e -> {
                cmdService.executeCommand(new CmdAddWatchListEntry(e, app));
                contentTable.getItems().remove(anime);
                updateTabTitle();
            });
        });

        final Button btnAddToFilterList = new Button(EMPTY, createIconFilterList());
        btnAddToFilterList.setTooltip(new Tooltip("add entry to filter list"));
        btnAddToFilterList.setOnAction(event -> {
            FilterEntry.valueOf(anime).ifPresent(e -> {
                cmdService.executeCommand(new CmdAddFilterEntry(e, app));
                contentTable.getItems().remove(anime);
                updateTabTitle();
            });
        });

        final Button removeButton = new Button(EMPTY, createIconRemove());
        removeButton.setTooltip(new Tooltip("remove"));
        removeButton.setOnAction(event -> {
            contentTable.getItems().remove(anime);
            updateTabTitle();
        });


        final HBox hBox = new HBox();
        hBox.setStyle("-fx-alignment: CENTER");
        hBox.setSpacing(5.0);
        hBox.getChildren().add(btnAddToFilterList);
        hBox.getChildren().add(btnAddToWatchlist);
        hBox.getChildren().add(removeButton);

        return hBox;
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
            contentTable.getItems().add((Anime) object);
            updateTabTitle();
        }

        if (observable instanceof TagRetrievalService && object instanceof Boolean) {
            Platform.runLater(() -> {
                hBoxProgress.setVisible(false);
            });
        }
    }


    private void updateTabTitle() {
        Platform.runLater(() -> tab.setText(String.format("%s (%s)", TAG_LIST_TITLE, contentTable.getItems().size())));
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

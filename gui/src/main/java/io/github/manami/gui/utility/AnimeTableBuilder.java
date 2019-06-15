package io.github.manami.gui.utility;

import java.util.function.Function;

import io.github.manami.Main;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdAddFilterEntry;
import io.github.manami.core.commands.CmdAddWatchListEntry;
import io.github.manami.core.commands.CommandService;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import static io.github.manami.gui.components.Icons.createIconFilterList;
import static io.github.manami.gui.components.Icons.createIconRemove;
import static io.github.manami.gui.components.Icons.createIconWatchList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class AnimeTableBuilder {

    private final ImageCache imageCache = Main.CONTEXT.getBean(ImageCache.class);
    private final CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);
    private final Manami app = Main.CONTEXT.getBean(Manami.class);

    private TableView table;
    private TableColumn<Anime, ImageView> colImage = new TableColumn<>("Image");
    private TableColumn<Anime, Hyperlink> colTitle = new TableColumn<>("Title");
    private TableColumn<Anime, HBox> colActions = new TableColumn<>("Actions");

    private Function<Anime, Image> pictureLoadFunction = imageCache::loadPicture;
    private Function<Anime, Void> listChangedFunction = (e) -> null;

    private boolean showAddToWatchListButton = true;
    private boolean showAddToFilterListButton = true;
    private boolean showRemoveButton = true;

    public AnimeTableBuilder(TableView table) {
        this.table = table;
        initDefaults();
    }

    private void initDefaults() {
        table.setStyle("-fx-selection-bar: #e1e7f5; -fx-selection-bar-non-focused: #ebebeb;");
        table.setCache(true);
        initImageColumnWithDefaults();
        initTitleColumnWithDefaults();
        initActionColumnWithDefaults();
    }

    private void initActionColumnWithDefaults() {
        colActions.setCellValueFactory(p -> new ReadOnlyObservableValue<HBox>() {

            @Override
            public HBox getValue() {
                return createActionButtons(p.getValue());
            }
        });
        colActions.setSortable(false);
        colActions.setEditable(false);
        table.getColumns().add(colActions);
    }

    private void initTitleColumnWithDefaults() {
        colTitle.setCellValueFactory(p -> new ReadOnlyObservableValue<Hyperlink>() {

            @Override
            public Hyperlink getValue() {
                Hyperlink title = HyperlinkBuilder.buildFrom(p.getValue().getTitle(), p.getValue().getInfoLink().toString());
                title.setFont(Font.font(24.0));
                return title;
            }
        });
        colTitle.setSortable(true);
        colTitle.setEditable(false);
        colTitle.setComparator((o1, o2) -> o1.getText().compareToIgnoreCase(o2.getText()));
        table.getColumns().add(colTitle);
    }

    private void initImageColumnWithDefaults() {
        colImage.setCellValueFactory(p -> new ReadOnlyObservableValue<ImageView>() {

            @Override
            public ImageView getValue() {
                ImageView cachedImageView = new ImageView(pictureLoadFunction.apply(p.getValue()));
                cachedImageView.setCache(true);
                return cachedImageView;
            }
        });
        colImage.setSortable(false);
        colImage.setEditable(false);
        table.getColumns().add(colImage);
    }

    private HBox createActionButtons(Anime anime) {
        final HBox hBox = new HBox();
        hBox.setStyle("-fx-alignment: CENTER");
        hBox.setSpacing(5.0);

        if (showAddToWatchListButton) {
            hBox.getChildren().add(createAddToWatchListButton(anime));
        }

        if (showAddToFilterListButton) {
            hBox.getChildren().add(createAddToFilterListButton(anime));
        }

        if (showRemoveButton) {
            hBox.getChildren().add(createRemoveButton(anime));
        }

        return hBox;
    }

    private Button createRemoveButton(Anime anime) {
        final Button removeButton = new Button(EMPTY, createIconRemove());
        removeButton.setTooltip(new Tooltip("remove"));
        removeButton.setOnAction(event -> {
            table.getItems().remove(anime);
            listChangedFunction.apply(anime);
        });
        return removeButton;
    }

    private Button createAddToFilterListButton(Anime anime) {
        final Button btnAddToFilterList = new Button(EMPTY, createIconFilterList());
        btnAddToFilterList.setTooltip(new Tooltip("add entry to filter list"));
        btnAddToFilterList.setOnAction(event -> {
            FilterEntry.valueOf(anime).ifPresent(e -> {
                cmdService.executeCommand(new CmdAddFilterEntry(e, app));
                table.getItems().remove(anime);
                listChangedFunction.apply(anime);
            });
        });
        return btnAddToFilterList;
    }

    private Button createAddToWatchListButton(Anime anime) {
        final Button btnAddToWatchlist = new Button(EMPTY, createIconWatchList());
        btnAddToWatchlist.setTooltip(new Tooltip("add to watch list"));
        btnAddToWatchlist.setOnAction(event -> {
            WatchListEntry.valueOf(anime).ifPresent(e -> {
                cmdService.executeCommand(new CmdAddWatchListEntry(e, app));
                table.getItems().remove(anime);
                listChangedFunction.apply(anime);
            });
        });

        return btnAddToWatchlist;
    }

    public AnimeTableBuilder withListChangedEvent(Function<Anime, Void> event) {
        this.listChangedFunction = event;
        return this;
    }

    public AnimeTableBuilder withTitleSortable(boolean value) {
        colTitle.setSortable(value);
        return this;
    }

    public AnimeTableBuilder withAddToWatchListButton(boolean value) {
        showAddToWatchListButton = value;
        return this;
    }

    public AnimeTableBuilder withAddToFilterListButton(boolean value) {
        showAddToFilterListButton = value;
        return this;
    }

    public AnimeTableBuilder withRemoveButton(boolean value) {
        showRemoveButton = value;
        return this;
    }
}
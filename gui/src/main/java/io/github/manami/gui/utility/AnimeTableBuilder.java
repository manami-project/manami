package io.github.manami.gui.utility;

import java.util.function.Function;

import io.github.manami.Main;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdAddFilterEntry;
import io.github.manami.core.commands.CmdAddWatchListEntry;
import io.github.manami.core.commands.CommandService;
import io.github.manami.dto.entities.FilterListEntry;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import static io.github.manami.gui.components.Icons.createIconDelete;
import static io.github.manami.gui.components.Icons.createIconFilterList;
import static io.github.manami.gui.components.Icons.createIconRemove;
import static io.github.manami.gui.components.Icons.createIconWatchList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class AnimeTableBuilder<T extends MinimalEntry> {

    private final static double COLUMN_SPACER = 20.0;

    private final ImageCache imageCache = Main.CONTEXT.getBean(ImageCache.class);
    private final CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);
    private final Manami app = Main.CONTEXT.getBean(Manami.class);
    private final SimpleDoubleProperty imageColWidth = new SimpleDoubleProperty(0.0);
    private final SimpleDoubleProperty titleColWidth = new SimpleDoubleProperty(0.0);
    private final SimpleDoubleProperty actionColWidth = new SimpleDoubleProperty(0.0);

    private TableView<T> table;
    private TableColumn<T, ImageView> colImage = new TableColumn<>("Image");
    private TableColumn<T, Hyperlink> colTitle = new TableColumn<>("Title");
    private TableColumn<T, HBox> colActions = new TableColumn<>("Actions");

    private Function<T, Image> pictureLoadFunction = imageCache::loadThumbnail;
    private Function<T, Void> listChangedFunction = (e) -> null;
    private Function<T, Void> deleteFunction;

    private boolean showAddToWatchListButton = true;
    private boolean showAddToFilterListButton = true;
    private boolean showRemoveButton = true;

    public AnimeTableBuilder(TableView<T> table) {
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

    private void addWidthListenerForTitleCol() {
        ChangeListener<Number> listener = ((observable, oldValue, newValue) -> {
            titleColWidth.set(table.getWidth() - imageColWidth.get() - actionColWidth.get() - 10);
        });

        imageColWidth.addListener(listener);
        actionColWidth.addListener(listener);
    }

    private void initActionColumnWithDefaults() {
        colActions.setCellValueFactory(p -> new ReadOnlyObservableValue<HBox>() {

            @Override
            public HBox getValue() {
                HBox actionButtons = createActionButtons(p.getValue());
                actionButtons.widthProperty().addListener((observable, oldValue, newValue) -> {
                    if (actionColWidth.get() < newValue.doubleValue()) {
                        actionColWidth.set(newValue.doubleValue() + COLUMN_SPACER);
                    }
                });

                return actionButtons;
            }
        });
        colActions.setSortable(false);
        colActions.setEditable(false);
        colActions.setResizable(false);
        colActions.prefWidthProperty().bind(actionColWidth);
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
        colTitle.setResizable(false);
        colTitle.prefWidthProperty().bind(titleColWidth);
        colTitle.setStyle("-fx-alignment: CENTER-LEFT;");
        colTitle.setComparator((o1, o2) -> o1.getText().compareToIgnoreCase(o2.getText()));
        table.getColumns().add(colTitle);

        addWidthListenerForTitleCol();
    }

    private void initImageColumnWithDefaults() {
        colImage.setCellValueFactory(p -> new ReadOnlyObservableValue<ImageView>() {

            @Override
            public ImageView getValue() {
                Image image = pictureLoadFunction.apply(p.getValue());
                ImageView cachedImageView = new ImageView(image);
                cachedImageView.setCache(true);

                image.widthProperty().addListener((observable, oldValue, newValue) -> {
                    if (imageColWidth.get() < newValue.doubleValue()) {
                        imageColWidth.set(newValue.doubleValue() + COLUMN_SPACER);
                    }
                });

                return cachedImageView;
            }
        });
        colImage.setSortable(false);
        colImage.setEditable(false);
        colImage.setResizable(false);
        colImage.prefWidthProperty().bind(imageColWidth);
        table.getColumns().add(colImage);
    }

    private HBox createActionButtons(T anime) {
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

        if (deleteFunction != null) {
            hBox.getChildren().add(createDeleteButton(anime));
        }

        return hBox;
    }

    private Button createDeleteButton(T anime) {
        final Button removeButton = new Button(EMPTY, createIconDelete());
        removeButton.setTooltip(new Tooltip("delete from list"));
        removeButton.setOnAction(event -> {
            deleteFunction.apply(anime);
        });

        return removeButton;
    }

    private Button createRemoveButton(T anime) {
        final Button removeButton = new Button(EMPTY, createIconRemove());
        removeButton.setTooltip(new Tooltip("remove"));
        removeButton.setOnAction(event -> {
            table.getItems().remove(anime);
            listChangedFunction.apply(anime);
        });
        return removeButton;
    }

    private Button createAddToFilterListButton(T anime) {
        final Button btnAddToFilterList = new Button(EMPTY, createIconFilterList());
        btnAddToFilterList.setTooltip(new Tooltip("add entry to filter list"));
        btnAddToFilterList.setOnAction(event -> {
            FilterListEntry.valueOf(anime).ifPresent(e -> {
                cmdService.executeCommand(new CmdAddFilterEntry(e, app));
                table.getItems().remove(anime);
                listChangedFunction.apply(anime);
            });
        });
        return btnAddToFilterList;
    }

    private Button createAddToWatchListButton(T anime) {
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

    public AnimeTableBuilder<T> withPicture(Function<T, Image> pictureLoadFunction) {
        this.pictureLoadFunction = pictureLoadFunction;
        return this;
    }

    public AnimeTableBuilder<T> withListChangedEvent(Function<T, Void> listChangedFunction) {
        table.getItems().addListener((ListChangeListener<T>) listener -> listChangedFunction.apply(null));
        return this;
    }

    public AnimeTableBuilder<T> withTitleSortable(boolean value) {
        colTitle.setSortable(value);
        return this;
    }

    public AnimeTableBuilder<T> withAddToWatchListButton(boolean value) {
        showAddToWatchListButton = value;
        return this;
    }

    public AnimeTableBuilder<T> withAddToFilterListButton(boolean value) {
        showAddToFilterListButton = value;
        return this;
    }

    public AnimeTableBuilder<T> withRemoveButton(boolean value) {
        showRemoveButton = value;
        return this;
    }

    public AnimeTableBuilder<T> withDeleteButton(Function<T, Void> event) {
        deleteFunction = event;
        return this;
    }
}
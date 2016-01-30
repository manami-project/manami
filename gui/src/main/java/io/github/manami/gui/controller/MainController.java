package io.github.manami.gui.controller;

import io.github.manami.Main;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdChangeEpisodes;
import io.github.manami.core.commands.CmdChangeInfoLink;
import io.github.manami.core.commands.CmdChangeLocation;
import io.github.manami.core.commands.CmdChangeTitle;
import io.github.manami.core.commands.CmdChangeType;
import io.github.manami.core.commands.CmdDeleteAnime;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.commands.ReversibleCommand;
import io.github.manami.core.config.Config;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.comparator.MinimalEntryComByTitleAsc;
import io.github.manami.dto.entities.Anime;
import io.github.manami.gui.components.Icons;
import io.github.manami.gui.controller.callbacks.AnimeEpisodesCallback;
import io.github.manami.gui.controller.callbacks.AnimeTypeCallback;
import io.github.manami.gui.controller.callbacks.DefaultCallback;
import io.github.manami.gui.controller.callbacks.RowCountCallback;
import io.github.manami.gui.utility.DialogLibrary;
import io.github.manami.gui.wrapper.CheckListControllerWrapper;
import io.github.manami.gui.wrapper.FilterListControllerWrapper;
import io.github.manami.gui.wrapper.MainControllerWrapper;
import io.github.manami.gui.wrapper.NewEntryControllerWrapper;
import io.github.manami.gui.wrapper.RecommendationsControllerWrapper;
import io.github.manami.gui.wrapper.RelatedAnimeControllerWrapper;
import io.github.manami.gui.wrapper.SearchResultsControllerWrapper;
import io.github.manami.gui.wrapper.WatchListControllerWrapper;
import io.github.manami.persistence.utility.PathResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Callback;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Controller for the main stage.
 *
 * @author manami-project
 * @since 2.0.0
 */
public class MainController implements Observer {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    /** Instance of the main application. */
    final private Manami app = Main.CONTEXT.getBean(Manami.class);

    /** Instance of the main application. */
    final private CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);

    /** Instance of the gui configuration. */
    final private MainControllerWrapper mainControllerWrapper = Main.CONTEXT.getBean(MainControllerWrapper.class);

    /** Instance of the application configuration. */
    final private Config config = Main.CONTEXT.getBean(Config.class);

    /** Tab for the filter list. */
    private Tab filterTab;

    /** Tab for the related animes. */
    private Tab relatedAnimeTab;

    /** Tab for the recommendations. */
    private Tab recommendationsTab;

    /** Tab for the check list. */
    private Tab checkListTab;

    /** Tab for search results. */
    private Tab searchResultTab;

    private FilterListControllerWrapper controllerWrapper;

    /** Tab for the watch list. */
    private Tab watchListTab;

    private AutoCompletionBinding<String> autoCompletionBinding;

    /** Table for anime list. */
    @FXML
    private TableView<Anime> tvAnimeList;

    /** Column: row number */
    @FXML
    private TableColumn<Anime, Anime> colAnimeListNumber;

    /** Column: title */
    @FXML
    private TableColumn<Anime, String> colAnimeListTitle;

    /** Column: type */
    @FXML
    private TableColumn<Anime, String> colAnimeListType;

    /** Column: episodes */
    @FXML
    private TableColumn<Anime, Integer> colAnimeListEpisodes;

    /** Column: info link */
    @FXML
    private TableColumn<Anime, String> colAnimeListLink;

    /** Column: location on hard drive */
    @FXML
    private TableColumn<Anime, String> colAnimeListLocation;

    /** Tab pane holding all tabs. */
    @FXML
    private TabPane tabPane;

    /** Tab for anime list. */
    @FXML
    private Tab tabAnimeList;

    /** Menu item: "New List" */
    @FXML
    private MenuItem miNewList;

    /** Menu item: "New Entry" */
    @FXML
    private MenuItem miNewEntry;

    /** Menu item: "Open" */
    @FXML
    private MenuItem miOpen;

    /** Menu item: "Import" */
    @FXML
    private MenuItem miImport;

    /** Menu item: "Check List" */
    @FXML
    private MenuItem miCheckList;

    /** Menu item: "Save" */
    @FXML
    private MenuItem miSave;

    /** Menu item: "Save as" */
    @FXML
    private MenuItem miSaveAs;

    /** Menu item: "Exit" */
    @FXML
    private MenuItem miExit;

    /** Menu item: "Redo" */
    @FXML
    private MenuItem miRedo;

    /** Menu item: "Undo" */
    @FXML
    private MenuItem miUndo;

    /** Menu item: "Export" */
    @FXML
    private MenuItem miExport;

    /** Menu item: "Delete" */
    @FXML
    private MenuItem miDeleteEntry;

    /** Contextmenu item: "Delete" */
    @FXML
    private MenuItem cmiDeleteEntry;

    /** Menu item: "Related Animes" */
    @FXML
    private MenuItem miRelatedAnimes;

    /** Menu item: "Recommendations" */
    @FXML
    private MenuItem miRecommendations;

    /** Menu item: "Filter List" */
    @FXML
    private MenuItem miFilterList;

    /** Menu item: "Watch List" */
    @FXML
    private MenuItem miWatchList;

    /** Menu item: "About" */
    @FXML
    private MenuItem miAbout;

    /** Textfield for searching an anime. */
    @FXML
    private TextField txtSearchString;

    /** Button which starts the search. */
    @FXML
    private Button btnSearch;


    /**
     * Initializes the table view for the anime list. Including column mapping
     * an so on.
     *
     * @since 2.0.0
     */
    public void initialize() {
        initFilterTab();
        initMenuItemGlyphs();

        tabAnimeList.setOnSelectionChanged(event -> {
            if (tabAnimeList.isSelected()) {
                refreshEntriesInGui();
                checkGui();
            }
        });

        Platform.setImplicitExit(false);
        mainControllerWrapper.getMainStage().setOnCloseRequest(event -> {
            event.consume();
            exit();
        });

        // Quicker access the list.
        tvAnimeList.getItems().addListener((ListChangeListener<Anime>) event -> {

            while (event.next()) {
                if (!event.wasPermutated()) {
                    checkGui();
                }
            }
        });

        // Only show button for deletion if the animelist is focused
        tabAnimeList.setOnSelectionChanged(event -> miDeleteEntry.setDisable(!tabAnimeList.isSelected()));

        // Callbacks
        final Callback<TableColumn<Anime, String>, TableCell<Anime, String>> defaultCallback = new DefaultCallback();

        // COLUMN: Number
        colAnimeListNumber.setCellValueFactory(value -> new ReadOnlyObjectWrapper<>(value.getValue()));
        colAnimeListNumber.setCellFactory(new RowCountCallback());

        // COLUMN: Title
        colAnimeListTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAnimeListTitle.setComparator(String::compareToIgnoreCase);
        colAnimeListTitle.setCellFactory(defaultCallback);
        colAnimeListTitle.setOnEditCommit(event -> {
            final Anime selectedAnime = tvAnimeList.getItems().get(event.getTablePosition().getRow());
            final Anime oldValue = new Anime(selectedAnime.getId());
            final String newTitle = event.getNewValue().trim();
            Anime.copyAnime(selectedAnime, oldValue);
            executeCommand(new CmdChangeTitle(oldValue, newTitle, app));
            selectedAnime.setTitle(newTitle);
        });

        // COLUMN: Type
        colAnimeListType.setCellValueFactory(new PropertyValueFactory<>("typeAsString"));
        colAnimeListType.setCellFactory(new AnimeTypeCallback());
        colAnimeListType.setOnEditCommit(event -> {
            final Anime selectedAnime = tvAnimeList.getItems().get(event.getTablePosition().getRow());
            final Anime oldValue = new Anime(selectedAnime.getId());
            Anime.copyAnime(selectedAnime, oldValue);
            executeCommand(new CmdChangeType(oldValue, AnimeType.findByName(event.getNewValue()), app));
            selectedAnime.setType(AnimeType.findByName(event.getNewValue()));
        });

        // COLUMN: Episodes
        colAnimeListEpisodes.setCellValueFactory(new PropertyValueFactory<>("episodes"));
        colAnimeListEpisodes.setCellFactory(new AnimeEpisodesCallback());
        colAnimeListEpisodes.setOnEditCommit(event -> {
            final Anime selectedAnime = tvAnimeList.getItems().get(event.getTablePosition().getRow());
            final Anime oldValue = new Anime(selectedAnime.getId());
            Anime.copyAnime(selectedAnime, oldValue);
            executeCommand(new CmdChangeEpisodes(oldValue, event.getNewValue(), app));
            selectedAnime.setEpisodes(event.getNewValue());
        });

        // COLUMN: Link
        colAnimeListLink.setCellValueFactory(new PropertyValueFactory<>("infoLink"));
        colAnimeListLink.setCellFactory(defaultCallback);
        colAnimeListLink.setOnEditCommit(event -> {
            final Anime selectedAnime = tvAnimeList.getItems().get(event.getTablePosition().getRow());
            final Anime oldValue = new Anime(selectedAnime.getId());
            Anime.copyAnime(selectedAnime, oldValue);
            executeCommand(new CmdChangeInfoLink(oldValue, event.getNewValue(), app));
            selectedAnime.setInfoLink(event.getNewValue());
        });

        // COLUMN: Location
        colAnimeListLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colAnimeListLocation.setCellFactory(defaultCallback);
        colAnimeListLocation.setOnEditStart(event -> {
            final Path folder = DialogLibrary.showBrowseForFolderDialog(mainControllerWrapper.getMainStage());
            final String newLocation = PathResolver.buildRelativizedPath(folder.toString(), config.getFile().getParent());

            if (StringUtils.isNotBlank(newLocation)) {
                final Anime selectedAnime = tvAnimeList.getItems().get(event.getTablePosition().getRow());
                final Anime oldValue = new Anime(selectedAnime.getId());
                Anime.copyAnime(selectedAnime, oldValue);
                executeCommand(new CmdChangeLocation(oldValue, newLocation, app));
                selectedAnime.setLocation(newLocation);
            }
        });

        btnSearch.setOnAction(event -> search());
        txtSearchString.setOnAction(event -> search());
    }


    /**
     * @since 2.9.1
     */
    private void initMenuItemGlyphs() {
        miNewList.setGraphic(Icons.createIconFileText());
        miNewEntry.setGraphic(Icons.createIconFile());
        miOpen.setGraphic(Icons.createIconFolderOpen());
        miSave.setGraphic(Icons.createIconSave());
        miImport.setGraphic(Icons.createIconImport());
        miExport.setGraphic(Icons.createIconExport());
        miExit.setGraphic(Icons.createIconExit());
        miUndo.setGraphic(Icons.createIconUndo());
        miRedo.setGraphic(Icons.createIconRedo());
        miDeleteEntry.setGraphic(Icons.createIconDelete());
        miAbout.setGraphic(Icons.createIconQuestion());
        cmiDeleteEntry.setGraphic(Icons.createIconDelete());
    }


    /**
     * @since 2.9.0
     */
    private void search() {
        app.search(txtSearchString.getText());
        showSearchResultTab();
    }


    /**
     * Enables or disables those menu items which depend on the existence of
     * entries.
     *
     * @since 2.0.0
     */
    private void checkEntryRelevantMenuItems() {
        final boolean isAnimeListEmpty = app.fetchAnimeList().size() == 0;
        Platform.runLater(() -> {
            miCheckList.setDisable(isAnimeListEmpty);
            miRelatedAnimes.setDisable(isAnimeListEmpty);
            miRecommendations.setDisable(isAnimeListEmpty);
            miDeleteEntry.setDisable(isAnimeListEmpty);
            cmiDeleteEntry.setDisable(isAnimeListEmpty);
        });

        final boolean allListsEmpty = app.fetchAnimeList().size() == 0 && app.fetchWatchList().size() == 0 && app.fetchFilterList().size() == 0;
        Platform.runLater(() -> {
            miSave.setDisable(allListsEmpty);
            miSaveAs.setDisable(allListsEmpty);
            miExport.setDisable(allListsEmpty);
        });
    }


    /**
     * Checks whether to set the dirty flag or not.
     *
     * @since 2.0.0
     */
    private void checkDirtyFlagAnimeListTab() {
        Platform.runLater(() -> mainControllerWrapper.setDirty(cmdService.isUnsaved()));
    }


    /**
     * Consists of different aspects to check. It's possible that the GUI needs
     * to react to changing circumstances. This method sums up all the checks.
     *
     * @since 2.0.0
     */
    public void checkGui() {
        checkEntryRelevantMenuItems();
        checkCommandMenuItems();
        checkDirtyFlagAnimeListTab();
        FXCollections.sort(tvAnimeList.getItems(), new MinimalEntryComByTitleAsc());
        refreshTableView();
        updateAutoCompletion();
    }


    /**
     * This method check if the command stacks are empty and either dis- or
     * enables the corresponding menu items.
     *
     * @since 2.0.0
     */
    private void checkCommandMenuItems() {
        Platform.runLater(() -> {
            miUndo.setDisable(cmdService.isEmptyDoneCommands());
            miRedo.setDisable(cmdService.isEmptyUndoneCommands());
        });
    }


    /**
     * Deletes a specific entry.
     *
     * @since 2.0.0
     */
    public void deleteEntry() {
        final Anime entry = tvAnimeList.getSelectionModel().getSelectedItem();

        if (entry != null) {
            cmdService.executeCommand(new CmdDeleteAnime(entry, app));
            Platform.runLater(() -> {
                tvAnimeList.getSelectionModel().clearSelection();
                tvAnimeList.getItems().remove(entry);
            });
        }
    }


    /**
     * Undoes the last command.
     *
     * @since 2.0.0
     */
    public void undo() {
        cmdService.undo();
        refreshEntriesInGui();
    }


    /**
     * Redoes the last undone command.
     *
     * @since 2.0.0
     */
    public void redo() {
        cmdService.redo();
        refreshEntriesInGui();
    }


    /**
     * Exports the current list.
     *
     * @since 2.0.0
     */
    public void export() {
        final Path file = DialogLibrary.showExportDialog(mainControllerWrapper.getMainStage());

        if (file != null) {
            app.export(file);
        }
    }

    /**
     * Interface for operations that need a secure execution context.
     *
     * @since 2.0.0
     */
    private interface ExecutionContext {

        void execute(Path file);
    }


    /**
     * Checks whether the current state is dirty and only executes the given
     * Method if it's not.
     *
     * @since 2.0.0
     * @param execCtx
     *            Lambda function trigger
     * @param file
     *            File.
     */
    private void safelyExecuteMethod(final ExecutionContext execCtx, final Path file) {
        final int userSelection = (cmdService.isUnsaved()) ? DialogLibrary.showUnsavedChangesDialog() : 0;

        switch (userSelection) {
            case 1:
                save();
            case 0:
                tabPane.getSelectionModel().select(tabAnimeList);
                execCtx.execute(file);
                break;
            default:
                break;
        }
    }


    /**
     * Creates a new, empty list.
     *
     * @since 2.0.0
     */
    public void newList() {
        safelyExecuteMethod(file -> {
            focusActiveTab(tabAnimeList);
            Platform.runLater(() -> {
                tabPane.getTabs().remove(filterTab);
                tabPane.getTabs().remove(relatedAnimeTab);
                tabPane.getTabs().remove(recommendationsTab);
                tabPane.getTabs().remove(checkListTab);
            });
            cancelAndResetBackgroundServices();
            app.newList();
        }, null);
    }


    /**
     * Opens a new file.
     *
     * @since 2.0.0
     */
    public void open() {
        final Path selectedFile = DialogLibrary.showOpenFileDialog(mainControllerWrapper.getMainStage());

        if (selectedFile != null && Files.exists(selectedFile)) {
            safelyExecuteMethod(file -> {
                cancelAndResetBackgroundServices();
                try {
                    app.newList();
                    app.open(file);
                    updateAutoCompletion();
                    refreshEntriesInGui();
                    controllerWrapper.startRecommendedFilterEntrySearch();
                } catch (final Exception e) {
                    LOG.error("An error occurred while trying to open {}: ", file, e);
                    DialogLibrary.showExceptionDialog(e);
                }
            }, selectedFile);
        }
    }


    /**
     * @since 2.9.1
     */
    private void updateAutoCompletion() {
        if (autoCompletionBinding != null) {
            autoCompletionBinding.dispose();
        }
        final List<String> suggestions = Lists.newArrayList();
        app.fetchAnimeList().forEach(e -> suggestions.add(e.getTitle()));
        app.fetchFilterList().forEach(e -> suggestions.add(e.getTitle()));
        app.fetchWatchList().forEach(e -> suggestions.add(e.getTitle()));
        autoCompletionBinding = TextFields.bindAutoCompletion(txtSearchString, suggestions);
    }


    /**
     * Imports a file.
     *
     * @since 2.0.0
     */
    public void importFile() {
        final Path selectedFile = DialogLibrary.showImportFileDialog(mainControllerWrapper.getMainStage());

        if (selectedFile != null && Files.exists(selectedFile)) {
            safelyExecuteMethod(file -> {
                app.importFile(file);
                refreshEntriesInGui();
            }, selectedFile);
        }
    }


    /**
     * Saves an already opened file or shows the save as dialog if the file does
     * not exist yet.
     *
     * @since 2.0.0
     */
    public void save() {
        final Path file = config.getFile();

        if (file != null) {
            app.save();
        } else {
            saveAs();
        }

        checkGui();
    }


    /**
     * Show a save as dialog and then saves the data to the file.
     *
     * @since 2.0.0
     */
    public void saveAs() {
        Path file = DialogLibrary.showSaveAsFileDialog(mainControllerWrapper.getMainStage());
        final String extension = ".xml";

        if (file != null) {
            final String filename = file.toString();

            if (!filename.endsWith(extension)) {
                file = Paths.get(filename + extension);
            }

            config.setFile(file);
            save();
        }
    }


    /**
     * Workaround to refresh the table view. This especially comes in handy
     * whenever you update an existing item.
     *
     * @since 2.0.0
     */
    private void refreshTableView() {
        Platform.runLater(() -> {
            for (int i = 0; i < tvAnimeList.getColumns().size(); i++) {
                tvAnimeList.getColumns().get(i).setVisible(false);
                tvAnimeList.getColumns().get(i).setVisible(true);
            }
        });
    }


    /**
     * Terminates the application.
     *
     * @since 2.0.0
     */
    public void exit() {
        safelyExecuteMethod(file -> {
            app.exit();
            Platform.exit();
        }, null);
    }


    /**
     * Sets the focus of the {@link TabPane} to the given {@link Tab}.
     *
     * @since 2.2.0
     * @param activeTab
     *            {@link Tab} which will gain focus.
     */
    private void focusActiveTab(final Tab activeTab) {
        Platform.runLater(() -> {
            if (!tabPane.getTabs().contains(activeTab)) {
                tabPane.getTabs().add(activeTab);
            }

            tabPane.getSelectionModel().select(activeTab);
        });
    }


    /**
     * Initializes the filter tab, as well as starts the filter list
     * recommendation search.
     *
     * @since 2.5.1
     */
    private void initFilterTab() {
        controllerWrapper = Main.CONTEXT.getBean(FilterListControllerWrapper.class);

        if (filterTab == null) {
            filterTab = controllerWrapper.getFilterTab();
        }
    }


    /**
     * Opens the filter list tab.
     *
     * @since 2.2.0
     */
    public void showFilterTab() {
        focusActiveTab(filterTab);
    }


    /**
     * Cancels and resets the related anime finder.
     *
     * @since 2.3.0
     */
    private void cancelAndResetBackgroundServices() {
        Main.CONTEXT.getBean(ServiceRepository.class).cancelAllServices();
    }


    /**
     * Opens the related anime tab.
     *
     * @since 2.3.0
     */
    public void showRelatedAnimeTab() {
        if (relatedAnimeTab == null) {
            relatedAnimeTab = Main.CONTEXT.getBean(RelatedAnimeControllerWrapper.class).getRelatedAnimeTab();
        }

        focusActiveTab(relatedAnimeTab);
    }


    public void showRecommendationsTab() {
        if (recommendationsTab == null) {
            recommendationsTab = Main.CONTEXT.getBean(RecommendationsControllerWrapper.class).getRecommendationsTab();
        }

        focusActiveTab(recommendationsTab);
    }


    /**
     * @since 2.6.0
     */
    public void showCheckListTab() {
        if (checkListTab == null) {
            checkListTab = Main.CONTEXT.getBean(CheckListControllerWrapper.class).getCheckListTab();
        }

        focusActiveTab(checkListTab);
    }


    public void showNewEntry() {
        Main.CONTEXT.getBean(NewEntryControllerWrapper.class).showNewEntryStage();
    }


    /**
     * Whenever a user clicks on a notification.
     *
     * @since 2.5.1
     */
    private void onClickOnNotification(final Tab tab) {
        if (tab != null) {
            mainControllerWrapper.getMainStage().toFront();
            mainControllerWrapper.getMainStage().requestFocus();
            focusActiveTab(tab);
        }
    }

    /**
     * An event which is handled whenever a notification for recommended filter
     * list entries is being clicked.
     *
     * @since 2.5.1
     * @author manami-project
     */
    class RecommendedFilterListEntryNotificationEventHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(final ActionEvent event) {
            onClickOnNotification(filterTab);
        }
    }

    /**
     * An event which is handled whenever a notification for related animes is
     * being clicked.
     *
     * @since 2.5.1
     * @author manami-project
     */
    class RelatedAnimeNotificationEventHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(final ActionEvent event) {
            onClickOnNotification(relatedAnimeTab);
        }
    }

    /**
     * An event which is handled whenever a notification for recommendations is
     * being clicked.
     *
     * @since 2.5.1
     * @author manami-project
     */
    class RecommendationsNotificationEventHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(final ActionEvent event) {
            onClickOnNotification(recommendationsTab);
        }
    }

    /**
     * An event which is handled whenever a notification for recommendations is
     * being clicked.
     *
     * @since 2.5.1
     * @author manami-project
     */
    class CheckListNotificationEventHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(final ActionEvent event) {
            onClickOnNotification(checkListTab);
        }
    }


    /**
     * @since 2.6.4
     * @param command
     */
    private void executeCommand(final ReversibleCommand command) {
        cmdService.executeCommand(command);
        checkGui();
    }


    @Override
    public void update(final Observable o, final Object object) {
        if (object == null) {
            checkGui();
            return;
        }

        if (object instanceof ReversibleCommand) {
            executeCommand((ReversibleCommand) object);
        }
    }


    /**
     * @since 2.7.0
     */
    public void refreshEntriesInGui() {
        Platform.runLater(() -> {
            tvAnimeList.getItems().clear();
            tvAnimeList.setItems(FXCollections.observableArrayList(app.fetchAnimeList()));
            autoSizeTableViewColumns();
        });
        checkGui();
    }


    /**
     * @since 2.7.2
     */
    public void showAbout() {
        Main.CONTEXT.getBean(AboutController.class).showAbout();
    }


    /**
     * @since 2.8.0
     */
    public void showWatchListTab() {
        if (watchListTab == null) {
            watchListTab = Main.CONTEXT.getBean(WatchListControllerWrapper.class).getWatchListTab();
        }

        focusActiveTab(watchListTab);
    }


    /**
     * @since 2.8.2
     * @return the tabAnimeList
     */
    public Tab getTabAnimeList() {
        return tabAnimeList;
    }


    /**
     * Resizes a {@link TableView} so that the column size automatically fits
     * it's content.
     *
     * @since 2.8.2
     */
    private void autoSizeTableViewColumns() {
        final List<TableColumn<Anime, ?>> colList = tvAnimeList.getColumns();

        for (final TableColumn<Anime, ?> tableColumn : colList) {
            final String longestText = determineLongestText(tableColumn);
            final Text text = new Text(longestText);
            final double textWidth = text.getLayoutBounds().getWidth();
            double newWidth = (textWidth < tableColumn.getMinWidth()) ? tableColumn.getMinWidth() : textWidth;
            // add a little spacer otherwise it's too narrow
            newWidth += 20.0;
            tableColumn.setPrefWidth(newWidth);
        }
    }


    /**
     * Determines the longest string within a column and returns it.
     *
     * @since 2.8.2
     * @return The longest string by chars of this column.
     */
    private String determineLongestText(final TableColumn<Anime, ?> tableColumn) {
        String ret = tableColumn.getText(); // init with header
        final Callback cellFactory = tableColumn.getCellFactory();
        final TableCell cell = (TableCell) cellFactory.call(tableColumn);
        cell.updateTableView(tableColumn.getTableView());
        cell.updateTableColumn(tableColumn);

        for (int index = 0; index < tableColumn.getTableView().getItems().size(); index++) {
            cell.updateIndex(index);

            final String cellContent = cell.getText();
            if (cellContent.length() > ret.length()) {
                ret = cellContent;
            }
        }

        return ret;
    }


    /**
     * Opens the related anime tab.
     *
     * @since 2.9.0
     */
    public void showSearchResultTab() {
        if (searchResultTab == null) {
            searchResultTab = Main.CONTEXT.getBean(SearchResultsControllerWrapper.class).getSearchResultsTab();
        }

        focusActiveTab(searchResultTab);
    }
}

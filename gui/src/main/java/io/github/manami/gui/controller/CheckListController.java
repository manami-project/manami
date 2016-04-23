package io.github.manami.gui.controller;

import static io.github.manami.core.config.Config.NOTIFICATION_DURATION;
import static io.github.manami.gui.components.Icons.createIconCancel;
import static io.github.manami.gui.components.Icons.createIconEdit;
import static io.github.manami.gui.components.Icons.createIconRemove;
import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Observable;
import java.util.Observer;

import org.controlsfx.control.Notifications;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.config.CheckListConfig;
import io.github.manami.core.config.Config;
import io.github.manami.core.services.CheckListService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.core.services.events.AbstractEvent.EventType;
import io.github.manami.core.services.events.CrcEvent;
import io.github.manami.core.services.events.Event;
import io.github.manami.core.services.events.ProgressState;
import io.github.manami.core.services.events.ReversibleCommandEvent;
import io.github.manami.gui.components.CheckListEntry;
import io.github.manami.gui.utility.HyperlinkBuilder;
import io.github.manami.gui.wrapper.MainControllerWrapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;

/**
 * @author manami-project
 * @since 2.6.0
 */
@Slf4j
public class CheckListController implements Observer {

    public static final String CHECK_LIST_TAB_TITLE = "Check List";

    /** Instance of the service repository. */
    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);

    private final CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);

    /** Instance of the application configuration. */
    final private Config appConfig = Main.CONTEXT.getBean(Config.class);

    /** Container holding all the progress components. */
    @FXML
    private VBox vBoxProgress;

    /** Progress bar */
    @FXML
    private ProgressBar progressBar;

    /** Label showing how many entries have been processed. */
    @FXML
    private Label lblProgress;

    /** Button for starting the search. */
    @FXML
    private Button btnStart;

    /** Button to cancel the service. */
    @FXML
    private Button btnCancel;

    /** {@link GridPane} containing the results. */
    @FXML
    private GridPane gridPane;

    @FXML
    private CheckBox cbLocations;

    @FXML
    private CheckBox cbCrc;

    @FXML
    private CheckBox cbMetaData;

    /** Instance of the tab in which the pane is being shown. */
    private Tab tab;

    /** Service instance. */
    private CheckListService service;

    private int amountOfEntries = 0;


    /**
     * Called from FXML when creating the Object.
     *
     * @since 2.4.0
     */
    public void initialize() {
        btnStart.setOnAction(event -> start());

        btnCancel.setGraphic(createIconCancel());
        btnCancel.setTooltip(new Tooltip("cancel"));
        btnCancel.setOnAction(event -> cancel());
    }


    private void showProgressControls(final boolean value) {
        Platform.runLater(() -> {
            vBoxProgress.setVisible(value);
            btnCancel.setVisible(value);
            btnStart.setVisible(!value);
        });
    }


    private void start() {
        clear();

        if (cbLocations.isSelected() || cbCrc.isSelected() || cbMetaData.isSelected()) {
            showProgressControls(true);
            final CheckListConfig config = new CheckListConfig(cbLocations.isSelected(), cbCrc.isSelected(), cbMetaData.isSelected());
            service = new CheckListService(config, appConfig.getFile(), Main.CONTEXT.getBean(Cache.class), Main.CONTEXT.getBean(Manami.class), this);
            serviceRepo.startService(service);
        }
    }


    /**
     * Stops the service if necessary and resets the GUI.
     *
     * @since 2.4.0
     */
    public void cancel() {
        if (service != null) {
            service.cancel();
        }
    }


    @Override
    public void update(final Observable observable, final Object object) {
        if (object == null) {
            return;
        }

        // it's an update of the progress
        if (object instanceof ProgressState) {
            final ProgressState state = (ProgressState) object;
            final int done = state.getDone();
            final int all = state.getTodo();
            final double percent = ((done * 100.00) / all) / 100.00;

            Platform.runLater(() -> {
                progressBar.setProgress(percent);
                lblProgress.setText(String.format("%s / %s", state.getDone(), all));
            });
        }

        if (object instanceof Event) {
            addEventEntry((Event) object);
            updateTabTitle();
        }

        if (object instanceof Boolean) {
            showProgressControls(false);
            Platform.runLater(() -> Notifications.create().title("Check List finished").text("Checking the list just finished.").hideAfter(NOTIFICATION_DURATION)
                    .onAction(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainController().new CheckListNotificationEventHandler()).showInformation());
        }
    }


    private ImageView createIcon(final EventType eventType) {
        final Image image = new Image(CheckListController.class.getResource("/icons/" + eventType.toString() + ".png").toString());
        final ImageView view = new ImageView(image);
        view.setFitWidth(25.0);
        view.setFitHeight(25.0);
        view.setSmooth(true);
        return view;
    }


    /**
     * @param entry
     */
    private void addComponentListEntryToGridPane(final CheckListEntry entry) {
        Platform.runLater(() -> {
            gridPane.getRowConstraints().add(new RowConstraints());
            final int rowNumber = gridPane.getRowConstraints().size() - 1;

            gridPane.add(entry.getPictureComponent(), 0, rowNumber);
            gridPane.add(entry.getTitleComponent(), 1, rowNumber);
            gridPane.add(entry.getMessageComponent(), 2, rowNumber);
            gridPane.add(entry.getAdditionalButtons(), 3, rowNumber);

            final Button removeButton = new Button("", createIconRemove());
            entry.setRemoveButton(removeButton);
            removeButton.setTooltip(new Tooltip("remove"));
            removeButton.setOnAction(event -> {
                removeEntry(entry);
            });

            gridPane.add(removeButton, 4, rowNumber);
            amountOfEntries++;
        });
    }


    /**
     * @param entry
     * @param hbox
     * @param removeButton
     */
    private void removeEntry(final CheckListEntry entry) {
        Platform.runLater(() -> {
            amountOfEntries--;
            gridPane.getChildren().remove(entry.getPictureComponent());
            gridPane.getChildren().remove(entry.getTitleComponent());
            gridPane.getChildren().remove(entry.getMessageComponent());
            gridPane.getChildren().remove(entry.getAdditionalButtons());
            gridPane.getChildren().remove(entry.getRemoveButton());
            updateTabTitle();
        });
    }


    private void addEventEntry(final Event event) {
        final CheckListEntry componentListEntry = new CheckListEntry();
        componentListEntry.setPictureComponent(createIcon(event.getType()));
        final Font titleFont = Font.font(null, FontWeight.BOLD, 11);

        if (event.getAnime() != null && isNotBlank(event.getAnime().getInfoLink())) {
            final Hyperlink title = HyperlinkBuilder.buildFrom(event.getTitle(), event.getAnime().getInfoLink());
            title.setFont(titleFont);
            componentListEntry.setTitleComponent(title);
        } else {
            final Label lblTitle = new Label(event.getTitle());
            lblTitle.setFont(titleFont);
            componentListEntry.setTitleComponent(lblTitle);
        }

        final Label lblMessage = new Label(event.getMessage());
        lblMessage.setFont((Font.font(11.5)));
        lblMessage.setWrapText(true);
        componentListEntry.setMessageComponent(lblMessage);

        if (event instanceof CrcEvent) {
            addCrcEventButton((CrcEvent) event, componentListEntry);
        }

        if (event instanceof ReversibleCommandEvent) {
            addReversibleCommandEventButton((ReversibleCommandEvent) event, componentListEntry);
        }

        addComponentListEntryToGridPane(componentListEntry);
    }


    private void addReversibleCommandEventButton(final ReversibleCommandEvent event, final CheckListEntry componentListEntry) {
        final Button button = new Button("", createIconEdit());
        button.setTooltip(new Tooltip("update"));

        button.setOnAction(trigger -> {
            cmdService.executeCommand(event.getCommand());
            removeEntry(componentListEntry);
        });
        componentListEntry.addAdditionalButtons(button);
    }


    private void updateTabTitle() {
        Platform.runLater(() -> tab.setText(String.format("%s (%s)", CHECK_LIST_TAB_TITLE, amountOfEntries)));
    }


    private void addCrcEventButton(final CrcEvent event, final CheckListEntry componentListEntry) {
        if (event.getPath() != null) {

            final Button button = new Button("", createIconEdit());
            button.setTooltip(new Tooltip("add CRC sum"));

            button.setOnAction(trigger -> {
                final Path file = event.getPath();
                final String formattedCrcSum = String.format("_[%s]", event.getCrcSum());
                final StringBuilder strBuilder = new StringBuilder(file.getFileName().toString());
                strBuilder.insert(strBuilder.lastIndexOf("."), formattedCrcSum);
                try {
                    Files.move(file, file.resolveSibling(strBuilder.toString()));
                    removeEntry(componentListEntry);
                } catch (final Exception e) {
                    log.error("An error occurred during renaming of the file {}", file.getFileName().toString(), e);
                    showExceptionDialog(e);
                }
            });
            componentListEntry.addAdditionalButtons(button);
        }
    }


    public void setTab(final Tab tab) {
        this.tab = tab;
    }


    /**
     * @since 2.8.2
     */
    public void clear() {
        Platform.runLater(() -> {
            tab.setText(CHECK_LIST_TAB_TITLE);
            gridPane.getChildren().clear();
            gridPane.getRowConstraints().clear();
            lblProgress.setText("");
            progressBar.setProgress(0.0);
            amountOfEntries = 0;
        });
        showProgressControls(false);
    }
}

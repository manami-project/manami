package io.github.manami.gui.controller;

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
import io.github.manami.gui.components.Icons;
import io.github.manami.gui.utility.DialogLibrary;
import io.github.manami.gui.utility.HyperlinkBuilder;
import io.github.manami.gui.wrapper.MainControllerWrapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author manami-project
 * @since 2.6.0
 */
public class CheckListController implements Observer {

    private static final Logger LOG = LoggerFactory.getLogger(CheckListController.class);

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

    /** List of all GUI components. */
    private List<CheckListEntry> componentList;


    /**
     * Called from FXML when creating the Object.
     *
     * @since 2.4.0
     */
    public void initialize() {
        componentList = Lists.newArrayList();
        btnStart.setOnAction(event -> start());

        btnCancel.setGraphic(Icons.createIconCancel());
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
        if (cbLocations.isSelected() || cbCrc.isSelected() || cbMetaData.isSelected()) {
            showProgressControls(true);
            componentList.clear();
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

        clear();
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
            Platform.runLater(() -> Notifications.create().title("Check List finished").text("Checking the list just finished.").hideAfter(Config.NOTIFICATION_DURATION)
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
     * Adds all {@link CheckListEntry}s to the {@link GridPane}.
     *
     * @since 2.1.3
     */
    protected void showEntries() {
        Platform.runLater(() -> {
            componentList.sort((a, b) -> {
                final String titleA = (a.getTitleComponent() instanceof Hyperlink) ? ((Hyperlink) a.getTitleComponent()).getText() : ((Label) a.getTitleComponent()).getText();
                final String titleB = (b.getTitleComponent() instanceof Hyperlink) ? ((Hyperlink) b.getTitleComponent()).getText() : ((Label) b.getTitleComponent()).getText();
                if (StringUtils.isNotBlank(titleA) && StringUtils.isNotBlank(titleB)) {
                    return Collator.getInstance().compare(titleA, titleB);
                }
                return 0;
            });

            gridPane.getChildren().clear();
            for (final CheckListEntry entry : componentList) {
                final RowConstraints row = new RowConstraints();
                gridPane.getRowConstraints().add(row);
                gridPane.add(entry.getPictureComponent(), 0, gridPane.getRowConstraints().size() - 1);
                GridPane.setMargin(entry.getPictureComponent(), new Insets(0.0, 0.0, 10.0, 0.0));
                gridPane.add(entry.getTitleComponent(), 1, gridPane.getRowConstraints().size() - 1);
                GridPane.setMargin(entry.getTitleComponent(), new Insets(0.0, 0.0, 10.0, 15.0));
                gridPane.add(entry.getMessageComponent(), 2, gridPane.getRowConstraints().size() - 1);
                GridPane.setMargin(entry.getMessageComponent(), new Insets(0.0, 0.0, 10.0, 15.0));
                if (entry.getDeletionButton() != null) {
                    gridPane.add(entry.getDeletionButton(), 3, gridPane.getRowConstraints().size() - 1);
                    GridPane.setMargin(entry.getDeletionButton(), new Insets(0.0, 0.0, 10.0, 20.0));
                }

                final Button removeButton = new Button("", Icons.createIconRemove());
                removeButton.setTooltip(new Tooltip("remove"));

                removeButton.setOnAction(event -> {
                    componentList.remove(entry);
                    showEntries();
                    updateTabTitle();
                });
                gridPane.add(removeButton, 4, gridPane.getRowConstraints().size() - 1);
                GridPane.setMargin(removeButton, new Insets(0.0, 0.0, 10.0, 20.0));
            }
        });
    }


    private void addEventEntry(final Event event) {
        final CheckListEntry componentListEntry = new CheckListEntry();
        componentListEntry.setPictureComponent(createIcon(event.getType()));
        final Font titleFont = Font.font(null, FontWeight.BOLD, 11);

        if (StringUtils.isNotBlank(event.getAnime().getInfoLink())) {
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

        componentList.add(componentListEntry);
        showEntries();
    }


    private void addReversibleCommandEventButton(final ReversibleCommandEvent event, final CheckListEntry componentListEntry) {
        final Button button = new Button("", Icons.createIconEdit());
        button.setTooltip(new Tooltip("update"));

        button.setOnAction(trigger -> {
            cmdService.executeCommand(event.getCommand());
            componentList.remove(componentListEntry);
            showEntries();
            updateTabTitle();
        });
        componentListEntry.setDeletionButton(button);
    }


    private void updateTabTitle() {
        Platform.runLater(() -> tab.setText(String.format("%s (%s)", CHECK_LIST_TAB_TITLE, componentList.size())));
    }


    private void addCrcEventButton(final CrcEvent event, final CheckListEntry componentListEntry) {
        if (event.getPath() != null) {

            final Button button = new Button("", Icons.createIconEdit());
            button.setTooltip(new Tooltip("add CRC sum"));

            button.setOnAction(trigger -> {
                final Path file = event.getPath();
                final String formattedCrcSum = String.format("_[%s]", event.getCrcSum());
                final StringBuilder strBuilder = new StringBuilder(file.getFileName().toString());
                strBuilder.insert(strBuilder.lastIndexOf("."), formattedCrcSum);
                try {
                    Files.move(file, file.resolveSibling(strBuilder.toString()));
                    componentList.remove(componentListEntry);
                    showEntries();
                    updateTabTitle();
                } catch (final Exception e) {
                    LOG.error("An error occurred during renaming of the file {}", file.getFileName().toString(), e);
                    DialogLibrary.showExceptionDialog(e);
                }
            });
            componentListEntry.setDeletionButton(button);
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
            lblProgress.setText("");
            progressBar.setProgress(0.0);
        });
        componentList.clear();
        showProgressControls(false);
    }
}

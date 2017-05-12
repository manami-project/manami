package io.github.manami.gui.controller;

import static io.github.manami.gui.utility.DialogLibrary.showBrowseForFolderDialog;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.nio.file.Path;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdAddAnime;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.config.Config;
import io.github.manami.core.services.AnimeRetrievalService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.gui.wrapper.MainControllerWrapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Shows the window in which a new entry can be created.
 *
 * @author manami-project
 * @since 2.0.0
 */
public class NewEntryController implements Observer {

    /** {@link TextField} for the title. */
    @FXML
    private TextField txtTitle;

    /** {@link TextField} for the type. */
    @FXML
    private TextField txtType;

    /** {@link TextField} for the amount of episodes. */
    @FXML
    private TextField txtEpisodes;

    /** {@link TextField} for the info link. */
    @FXML
    private TextField txtInfoLink;

    /** {@link TextField} for the location on the hard drive. */
    @FXML
    private TextField txtLocation;

    /** {@link Button} for changing the type (up). */
    @FXML
    private Button btnTypeUp;

    /** {@link Button} for changing the type (down). */
    @FXML
    private Button btnTypeDown;

    /** {@link Button} for increasing the number of episodes. */
    @FXML
    private Button btnEpisodeUp;

    /** {@link Button} for decreasing the number of episodes. */
    @FXML
    private Button btnEpisodeDown;

    /** {@link Button} for adding the entry. */
    @FXML
    private Button btnAdd;

    /** {@link Button} to close the window. */
    @FXML
    private Button btnCancel;

    /** {@link Button} to browse for a location. */
    @FXML
    private Button btnBrowse;

    /** Command service. */
    private final CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);

    /** Instance of the service repository. */
    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);

    /** Context configuration. */
    private final Config config = Main.CONTEXT.getBean(Config.class);

    /** Current index of the spinner's textfield value. */
    private int typeIndex = 0;

    private ValidationSupport validationSupport;


    /**
     * Called on Construction
     *
     * @since 2.0.0
     */
    public void initialize() {
        validationSupport = new ValidationSupport();
        validationSupport.registerValidator(txtTitle, Validator.createEmptyValidator("Title is required"));
        validationSupport.registerValidator(txtLocation, Validator.createEmptyValidator("Location is required"));
        validationSupport.registerValidator(txtInfoLink, (c, value) -> {
            final String str = (String) value;
            if (isNotEmpty(str)) {
                if (!str.startsWith("http://")) {
                    if (!str.startsWith("https://")) {
                        return ValidationResult.fromError(txtInfoLink, "Info link must be a valid URL");
                    }
                }
            }
            return null;
        });

        txtType.setText(AnimeType.TV.getValue());
        txtEpisodes.focusedProperty().addListener((currentValue, valueBefore, valueAfter) -> {
            if (!NumberUtils.isParsable(txtEpisodes.getText()) || txtEpisodes.getText().startsWith("-") || "0".equals(txtEpisodes.getText())) {
                txtEpisodes.setText("1");
            } else {
                try {
                    Integer.parseInt(txtEpisodes.getText());
                } catch (final NumberFormatException e) {
                    txtEpisodes.setText("1");
                }

                final boolean deactivateDecreaseButton = Integer.parseInt(txtEpisodes.getText()) == 1;
                btnEpisodeDown.setDisable(deactivateDecreaseButton);
            }

        });

        txtInfoLink.focusedProperty().addListener((currentValue, valueBefore, valueAfter) -> {
            if (valueBefore && !valueAfter) {
                autoFillForm();
            }
        });
    }


    /**
     * Changes the type to the previous entry.
     *
     * @since 2.0.0
     */
    public void typeUp() {
        if (typeIndex > 0) {
            typeIndex--;
            Platform.runLater(() -> txtType.setText(AnimeType.values()[typeIndex].getValue()));
            checkTypeArrowButtons();
        }
    }


    /**
     * Changes the type to the next entry.
     *
     * @since 2.0.0
     */
    public void typeDown() {
        if (typeIndex < AnimeType.values().length - 1) {
            typeIndex++;
            Platform.runLater(() -> txtType.setText(AnimeType.values()[typeIndex].getValue()));
            checkTypeArrowButtons();
        }
    }


    /**
     * Increases the number of episodes by one.
     *
     * @since 2.0.0
     */
    public void increaseEpisodes() {
        final Integer value = Integer.valueOf(txtEpisodes.getText()) + 1;
        txtEpisodes.setText((value > 0) ? value.toString() : "1");
        if (btnEpisodeDown.isDisabled()) {
            Platform.runLater(() -> btnEpisodeDown.setDisable(false));
        }
    }


    /**
     * Decreases the number of episodes by one.
     *
     * @since 2.0.0
     */
    public void decreaseEpisodes() {
        final Integer value = Integer.valueOf(txtEpisodes.getText()) - 1;
        txtEpisodes.setText((value > 0) ? value.toString() : "1");
        if (value == 1) {
            Platform.runLater(() -> btnEpisodeDown.setDisable(true));
        }
    }


    /**
     * Closes the window without saving anything.
     *
     * @since 2.0.0
     */
    public void close() {
        Platform.runLater(() -> btnCancel.getParent().getScene().getWindow().hide());
    }


    /**
     * Adds a new entry to the list.
     *
     * @since 2.0.0
     */
    public void add() {
        final String title = txtTitle.getText().trim();
        final Integer episodes = Integer.valueOf(txtEpisodes.getText().trim());
        final InfoLink infoLink = new InfoLink(txtInfoLink.getText().trim());
        final String location = txtLocation.getText().trim();
        final String type = txtType.getText().trim();
        if (validationSupport.getValidationResult().getErrors().size() == 0) {
            cmdService.executeCommand(new CmdAddAnime(new Anime(title, infoLink).type(AnimeType.findByName(type)).episodes(episodes).location(location), Main.CONTEXT.getBean(Manami.class)));
            close();
        }
    }


    /**
     * Checks which button to activate or deactivate.
     *
     * @since 2.0.0
     */
    private void checkTypeArrowButtons() {
        final int upperLimit = AnimeType.values().length - 1;

        if (typeIndex == 0 && !btnTypeUp.isDisabled()) {
            Platform.runLater(() -> btnTypeUp.setDisable(true));
        } else if (typeIndex > 0 && btnTypeUp.isDisabled()) {
            Platform.runLater(() -> btnTypeUp.setDisable(false));
        }

        if (typeIndex == upperLimit && !btnTypeDown.isDisabled()) {
            Platform.runLater(() -> btnTypeDown.setDisable(true));
        } else if (typeIndex < upperLimit && btnTypeDown.isDisabled()) {
            Platform.runLater(() -> btnTypeDown.setDisable(false));
        }
    }


    /**
     * Checks whether or not to disable the episode decrease button.
     *
     * @since 2.2.0
     */
    private void checkEpisodeArrowButtons() {
        final int episodes = Integer.parseInt(txtEpisodes.getText());
        btnEpisodeUp.setDisable(false);

        if (episodes > 1) {
            Platform.runLater(() -> btnEpisodeDown.setDisable(false));
        } else {
            Platform.runLater(() -> btnEpisodeDown.setDisable(true));
        }
    }


    /**
     * Sets the type in the textfield based on the given value.
     *
     * @param value
     *            Type of the {@link Anime}
     */
    private void setTextfieldType(final String value) {
        String curListElement;
        for (int index = 0; index < AnimeType.values().length; index++) {
            curListElement = AnimeType.values()[index].getValue();
            if (curListElement.equalsIgnoreCase(value)) {
                typeIndex = index;
                txtType.setText(curListElement);
                break;
            }
        }
        checkTypeArrowButtons();
    }


    /**
     * Checks the currently given value of the textfield and tries to
     * automatically fill out the form.
     *
     * @since 2.0.0
     */
    private void autoFillForm() {
        AnimeRetrievalService retrievalService;

        convertUrlIfNecessary();

        final InfoLink infoLink = new InfoLink(txtInfoLink.getText().trim());

        if (infoLink.isValid()) {
            setDisableAutoCompleteWidgets(true);

            retrievalService = new AnimeRetrievalService(Main.CONTEXT.getBean(Cache.class), infoLink);
            retrievalService.addObserver(this);
            serviceRepo.startService(retrievalService);

        }
    }


    private void convertUrlIfNecessary() {
        final Pattern pattern = Pattern.compile("anime\\.php\\?id=[0-9]*");
        final Matcher matcher = pattern.matcher(txtInfoLink.getText());

        if (matcher.find()) {
            String id = matcher.group();
            id = id.replace("anime.php?id=", EMPTY);
            txtInfoLink.setText("http://myanimelist.net/anime/".concat(id));
        }
    }


    /**
     * Enables or disables all widgets on the scene which are filled by
     * autocomplete.
     *
     * @since 2.0.0
     * @param value
     *            Disables the component if the value is true and enables them
     *            if the value is false.
     */
    private void setDisableAutoCompleteWidgets(final boolean value) {
        Platform.runLater(() -> {
            txtTitle.setDisable(value);
            txtEpisodes.setDisable(value);
            txtInfoLink.setDisable(value);
            btnAdd.setDisable(value);
        });

        if (value) {
            Platform.runLater(() -> {
                btnTypeUp.setDisable(true);
                btnTypeDown.setDisable(true);
                btnEpisodeUp.setDisable(true);
                btnEpisodeDown.setDisable(true);
            });
        } else {
            checkTypeArrowButtons();
            checkEpisodeArrowButtons();
        }
    }


    public void browse() {
        final Path directory = showBrowseForFolderDialog(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainStage());
        String location;

        if (directory != null) {
            if (config.getFile() == null) {
                location = directory.toAbsolutePath().toString();
            } else {
                try {
                    location = config.getFile().getParent().relativize(directory).toString().replace("\\", "/");
                } catch (final IllegalArgumentException e) {
                    location = directory.toAbsolutePath().toString();
                }
            }

            txtLocation.setText(location);
        }
    }


    @Override
    public void update(final Observable observable, final Object object) {
        if (observable == null || object == null) {
            return;
        }

        if (observable instanceof AnimeRetrievalService && object instanceof Anime) {
            final Anime anime = (Anime) object;

            if (anime != null) {
                Platform.runLater(() -> {
                    txtTitle.setText(anime.getTitle());
                    txtEpisodes.setText(String.valueOf(anime.getEpisodes()));
                    txtInfoLink.setText(anime.getInfoLink().getUrl());
                    setTextfieldType(anime.getTypeAsString());
                    checkEpisodeArrowButtons();
                });
            }
            setDisableAutoCompleteWidgets(false);
        }
    }
}

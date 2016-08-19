package io.github.manami.gui.utility;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * Contains pre configured {@link FileChooser} dialogs.
 *
 * @author manami-project
 * @since 2.0.0
 */
public final class DialogLibrary {

    /** Instance of the {@link FileChooser} */
    private static FileChooser fileChooser;

    /** Extension for manami xml diles. */
    public final static ExtensionFilter XML_FILTER = new FileChooser.ExtensionFilter("XML", "*.xml");

    /** Extension for csv files. */
    public final static ExtensionFilter CSV_FILTER = new FileChooser.ExtensionFilter("CSV", "*.csv");

    /** Extension for json files. */
    public final static ExtensionFilter JSON_FILTER = new FileChooser.ExtensionFilter("JSON", "*.json");

    /** Extension for myanimelist.net xml files. */
    public final static ExtensionFilter XML_MAL_FILTER = new FileChooser.ExtensionFilter("myanimelist.net", "*.xml");


    /**
     * Opens a {@link FileChooser} for opening a file and handles all the logic.
     *
     * @since 2.0.0
     * @param stage
     *            The main stage.
     * @return {@link Path} of the file to open or null.
     */
    public static Path showOpenFileDialog(final Stage stage) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Select your anime list...");
        fileChooser.getExtensionFilters().addAll(XML_FILTER);

        final File ret = fileChooser.showOpenDialog(stage);
        return (ret != null) ? ret.toPath() : null;
    }


    /**
     * Opens a {@link FileChooser} for importing a file and handles all the
     * logic.
     *
     * @since 2.0.0
     * @param stage
     *            The main stage.
     * @return {@link Path} of the file to import or null.
     */
    public static Path showImportFileDialog(final Stage stage) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Import file...");
        fileChooser.getExtensionFilters().addAll(XML_MAL_FILTER, JSON_FILTER, CSV_FILTER);

        final File ret = fileChooser.showOpenDialog(stage);
        return (ret != null) ? ret.toPath() : null;
    }


    /**
     * Opens a {@link FileChooser} for saving a file and handles all the logic.
     *
     * @since 2.0.0
     * @param stage
     *            The main stage.
     * @return {@link Path} of the newly created file or null.
     */
    public static Path showSaveAsFileDialog(final Stage stage) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Save your anime list as...");
        fileChooser.getExtensionFilters().addAll(XML_FILTER);

        final File ret = fileChooser.showSaveDialog(stage);
        return (ret != null) ? ret.toPath() : null;
    }


    /**
     * Opens a {@link FileChooser} for exporting a file and handles all the
     * logic.
     *
     * @since 2.0.0
     * @param stage
     *            The main stage.
     * @return {@link Path} for the newly created file or null.
     */
    public static Path showExportDialog(final Stage stage) {
        return showExportDialog(stage, new ExtensionFilter[] { JSON_FILTER, CSV_FILTER });
    }


    /**
     * Opens a {@link FileChooser} for exporting a file and handles all the
     * logic.
     *
     * @since 2.10.0
     * @param stage
     *            The main stage.
     * @return {@link Path} for the newly created file or null.
     */
    public static Path showExportDialog(final Stage stage, final ExtensionFilter... filter) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Export anime list as...");
        fileChooser.getExtensionFilters().addAll(filter);

        final File ret = fileChooser.showSaveDialog(stage);
        return (ret != null) ? ret.toPath() : null;
    }


    /**
     * Shows the dialog in which the user has to choose between saving the
     * changes, not saving the changes and canceling the action.
     *
     * @since 2.0.0
     * @return A number which indicates whether to save or not. -1="cancel"
     *         0="don't save" 1="save"
     */
    public static int showUnsavedChangesDialog() {
        int ret = -1;
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Your changes will be lost if you don't save them.");
        alert.setContentText("Do you want to save your changes?");

        final ButtonType btnYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        final ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        final ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnYes, btnNo, btnCancel);

        final Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == btnYes) {
            ret = 1;
        } else if (result.get() == btnNo) {
            ret = 0;
        }

        return ret;
    }


    /**
     * @since 2.6.0
     * @param stage
     * @return
     */
    public static Path showBrowseForFolderDialog(final Stage stage) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Browse for directory...");

        final File ret = directoryChooser.showDialog(stage);
        return (ret != null) ? ret.toPath() : null;
    }


    /**
     * @since 2.8.2
     * @param exception
     */
    public static void showExceptionDialog(final Exception exception) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred:");
        alert.setContentText(exception.getMessage());

        // Create expandable Exception.
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        final String exceptionText = sw.toString();

        final TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        final GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }
}

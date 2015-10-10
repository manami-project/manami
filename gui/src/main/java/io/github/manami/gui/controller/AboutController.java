package io.github.manami.gui.controller;

import javafx.scene.control.Alert;
import io.github.manami.dto.ToolVersion;

import javax.inject.Named;

/**
 * Controller showing a window with application information.
 *
 * @author manami-project
 * @since 2.0.0
 */
@Named
public class AboutController {

    public void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Version: " + ToolVersion.getVersion());
        alert.setContentText("Free non-commercial software.");
        alert.showAndWait();
    }
}
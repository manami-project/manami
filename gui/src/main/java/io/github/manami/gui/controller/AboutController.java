package io.github.manami.gui.controller;

import io.github.manami.dto.ToolVersion;
import javafx.scene.control.Alert;

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
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(String.format("Version: %s", ToolVersion.getVersion()));
        alert.setContentText("Free non-commercial software. (AGPLv3)\n\nhttps://github.com/manami-project/manami");
        alert.showAndWait();
    }
}
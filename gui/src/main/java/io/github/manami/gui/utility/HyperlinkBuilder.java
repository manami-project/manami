package io.github.manami.gui.utility;

import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;
import static java.awt.Desktop.getDesktop;

import java.awt.Desktop;
import java.net.URI;

import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseButton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HyperlinkBuilder {

    public static Hyperlink buildFrom(final String title, final String url) {
        final Hyperlink ret = new Hyperlink(title);
        ret.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                try {
                    final Desktop desktop = getDesktop();
                    desktop.browse(new URI(url));
                } catch (final Exception e) {
                    log.error("An error occurred trying to open the infolink in the default browser: ", e);
                    showExceptionDialog(e);
                }
            }
        });
        return ret;
    }
}

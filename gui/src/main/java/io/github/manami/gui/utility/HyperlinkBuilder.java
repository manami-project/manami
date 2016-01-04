package io.github.manami.gui.utility;

import java.awt.Desktop;
import java.net.URI;

import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author manami-project
 * @since 2.10.0
 */
public class HyperlinkBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(HyperlinkBuilder.class);


    /**
     * @param title
     * @param url
     * @return
     * @since 2.10.0
     */
    public static Hyperlink buildFrom(final String title, final String url) {
        final Hyperlink ret = new Hyperlink(title);
        ret.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                try {
                    final Desktop desktop = java.awt.Desktop.getDesktop();
                    desktop.browse(new URI(url));
                } catch (final Exception e) {
                    LOG.error("An error occurred trying to open the infolink in the default browser: ", e);
                    DialogLibrary.showExceptionDialog(e);
                }
            }
        });
        return ret;
    }
}

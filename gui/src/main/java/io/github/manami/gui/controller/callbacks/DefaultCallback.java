package io.github.manami.gui.controller.callbacks;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import io.github.manami.dto.entities.Anime;

/**
 * Callback which is executed in case one of the simple {@link String} based
 * table cells has changed.
 */
public class DefaultCallback implements Callback<TableColumn<Anime, String>, TableCell<Anime, String>> {

    @Override
    public TableCell<Anime, String> call(final TableColumn<Anime, String> column) {
        return new TextFieldTableCell<Anime, String>(new DefaultStringConverter()) {

            @Override
            public void updateItem(final String value, final boolean empty) {
                super.updateItem(value, empty);
            }
        };
    }
}

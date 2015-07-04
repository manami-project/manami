package io.github.manami.gui.controller.callbacks;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;

/**
 * Callback which is executed whenever the type is being changed.
 * 
 * @author manami project
 * @since 2.0.0
 */
public class AnimeTypeCallback implements Callback<TableColumn<Anime, String>, TableCell<Anime, String>> {

    @Override
    public TableCell<Anime, String> call(final TableColumn<Anime, String> arg0) {
        final ChoiceBoxTableCell<Anime, String> cellFactory = new ChoiceBoxTableCell<Anime, String>(new DefaultStringConverter()) {

            @Override
            public void updateItem(final String value, final boolean empty) {
                super.updateItem(value, empty);
            }
        };
        cellFactory.getItems().clear();

        for (final AnimeType element : AnimeType.values()) {
            cellFactory.getItems().add(element.getValue());
        }

        return cellFactory;
    }
}

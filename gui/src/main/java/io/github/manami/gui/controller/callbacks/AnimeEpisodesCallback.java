package io.github.manami.gui.controller.callbacks;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import io.github.manami.dto.entities.Anime;

/**
 * Callback which is called whenever an episode was changes within the anime
 * list table.
 */
public class AnimeEpisodesCallback implements Callback<TableColumn<Anime, Integer>, TableCell<Anime, Integer>> {

    @Override
    public TableCell<Anime, Integer> call(final TableColumn<Anime, Integer> arg0) {
        return new TextFieldTableCell<Anime, Integer>(new IntegerStringConverter()) {

            @Override
            public void updateItem(final Integer value, final boolean empty) {
                super.updateItem(value, empty);
            }
        };
    }
}

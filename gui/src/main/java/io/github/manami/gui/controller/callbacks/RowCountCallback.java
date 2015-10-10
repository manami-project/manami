package io.github.manami.gui.controller.callbacks;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import io.github.manami.dto.entities.Anime;

/**
 * Callback for the non-editable column which shows the current row number.
 * 
 * @author manami-project
 * @since 2.0.0
 */
public class RowCountCallback implements Callback<TableColumn<Anime, Anime>, TableCell<Anime, Anime>> {

    @Override
    public TableCell<Anime, Anime> call(final TableColumn<Anime, Anime> arg0) {
        return new TableCell<Anime, Anime>() {

            @Override
            protected void updateItem(final Anime item, final boolean empty) {
                super.updateItem(item, empty);

                if (getTableRow() != null && item != null) {
                    final Integer index = getTableRow().getIndex() + 1;
                    setText(index.toString());
                } else {
                    setText("");
                }
            }
        };
    }
}

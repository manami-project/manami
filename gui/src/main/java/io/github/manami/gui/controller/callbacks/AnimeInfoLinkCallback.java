package io.github.manami.gui.controller.callbacks;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class AnimeInfoLinkCallback implements Callback<TableColumn<Anime, InfoLink>, TableCell<Anime, InfoLink>> {

    @Override
    public TableCell<Anime, InfoLink> call(final TableColumn<Anime, InfoLink> arg0) {
        return new TextFieldTableCell<>(new StringConverter<InfoLink>() {

            @Override
            public String toString(final InfoLink infoLink) {
                if (infoLink == null) {
                    return "";
                }

                return infoLink.getUrl();
            }


            @Override
            public InfoLink fromString(final String url) {
                return new InfoLink(url);
            }
        });
    }
}

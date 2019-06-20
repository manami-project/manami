package io.github.manami.dto.entities;

import java.util.Optional;

public class FilterListEntry extends AbstractMinimalEntry {

    public FilterListEntry(final String title, final String thumbnail, final InfoLink infoLink) {
        super.setTitle(title);
        super.setThumbnail(thumbnail);
        super.setInfoLink(infoLink);
    }


    public FilterListEntry(final String title, final InfoLink infoLink) {
        super.setTitle(title);
        super.setInfoLink(infoLink);
    }


    public static Optional<FilterListEntry> valueOf(final MinimalEntry anime) {
        if (anime == null) {
            return Optional.empty();
        }

        return Optional.of(new FilterListEntry(anime.getTitle(), anime.getThumbnail(), anime.getInfoLink()));
    }
}

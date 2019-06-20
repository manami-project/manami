package io.github.manami.dto.entities;

import java.util.Optional;

public class WatchListEntry extends AbstractMinimalEntry {

    public WatchListEntry(final String title, final String thumbnail, final InfoLink infoLink) {
        super.setTitle(title);
        super.setThumbnail(thumbnail);
        super.setInfoLink(infoLink);
    }


    public WatchListEntry(final String title, final InfoLink infoLink) {
        super.setTitle(title);
        super.setInfoLink(infoLink);
    }


    public static Optional<WatchListEntry> valueOf(final MinimalEntry anime) {
        if (anime == null) {
            return Optional.empty();
        }

        return Optional.of(new WatchListEntry(anime.getTitle(), anime.getThumbnail(), anime.getInfoLink()));
    }
}

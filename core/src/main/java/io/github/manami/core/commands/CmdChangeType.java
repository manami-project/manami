package io.github.manami.core.commands;

import static io.github.manami.dto.entities.Anime.copyAnime;

import io.github.manami.core.Manami;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;

/**
 * Command for changing the type.
 */
public class CmdChangeType extends AbstractReversibleCommand {

    /**
     * @param anime Anime to change
     * @param newValue The new value.
     * @param application Instance of the application which reveals access to the persistence functionality.
     */
    public CmdChangeType(final Anime anime, final AnimeType newValue, final Manami application) {
        app = application;
        oldAnime = anime;
        newAnime = copyAnime(oldAnime);
        newAnime.setType(newValue);
    }
}

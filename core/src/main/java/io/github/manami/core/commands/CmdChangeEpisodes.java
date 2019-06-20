package io.github.manami.core.commands;

import static io.github.manami.dto.entities.Anime.copyAnime;

import io.github.manami.core.Manami;
import io.github.manami.dto.entities.Anime;

/**
 * Command for changing the value of an episode.
 */
public class CmdChangeEpisodes extends AbstractReversibleCommand {

    /**
     * @param anime Anime that is being edited.
     * @param newValue The new title.
     * @param application Instance of the application which reveals access to the persistence functionality.
     */
    public CmdChangeEpisodes(final Anime anime, final int newValue, final Manami application) {
        app = application;
        oldAnime = anime;
        newAnime = copyAnime(oldAnime);
        newAnime.setEpisodes(newValue);
    }
}

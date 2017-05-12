package io.github.manami.core.commands;

import static io.github.manami.dto.entities.Anime.copyAnime;

import io.github.manami.core.Manami;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;

/**
 * Command for changing the info link.
 *
 * @author manami-project
 * @since 2.0.0
 */
public class CmdChangeInfoLink extends AbstractReversibleCommand {

    /**
     * Constructor
     *
     * @since 2.6.0
     * @param anime
     *            Anime to change.
     * @param newValue
     *            The new value.
     * @param application
     *            Instance of the application which reveals access to the
     *            persistence functionality.
     */
    public CmdChangeInfoLink(final Anime anime, final InfoLink newValue, final Manami application) {
        app = application;
        oldAnime = anime;
        newAnime = copyAnime(oldAnime);
        newAnime.setInfoLink(newValue);
    }
}

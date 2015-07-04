package io.github.manami.core.commands;

import io.github.manami.core.Manami;
import io.github.manami.dto.entities.Anime;

/**
 * Command for adding an entry.
 *
 * @author manami project
 * @since 2.0.0
 */
public class CmdAddAnime extends AbstractReversibleCommand {

    private final Manami app;


    /**
     * Constructor
     *
     * @since 2.0.0
     * @param entry Anime that is being added.
     * @param application Instance of the application which reveals access to the persistence functionality.
     */
    public CmdAddAnime(final Anime entry, final Manami application) {
        newAnime = entry;
        app = application;
    }


    @Override
    public boolean execute() {
        return app.addAnime(newAnime);
    }


    @Override
    public void undo() {
        app.removeAnime(newAnime.getId());
    }
}

package io.github.manami.core.commands;

import io.github.manami.core.Manami;
import io.github.manami.dto.entities.Anime;

/**
 * Command for deleting an entry.
 */
public class CmdDeleteAnime extends AbstractReversibleCommand {

    /**
     * @param entry {@link Anime} that is supposed to be deleted.
     */
    public CmdDeleteAnime(final Anime entry, final Manami application) {
        app = application;
        oldAnime = entry;
    }


    @Override
    public boolean execute() {
        return app.removeAnime(oldAnime.getId());
    }


    @Override
    public void undo() {
        app.addAnime(oldAnime);
    }
}

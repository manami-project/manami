package io.github.manami.core.commands;

import static io.github.manami.dto.entities.Anime.copyNullTarget;

import io.github.manami.core.Manami;
import io.github.manami.dto.entities.Anime;

/**
 * Abstract reversible command.
 *
 * @author manami-project
 * @since 2.0.0
 */
public abstract class AbstractReversibleCommand implements ReversibleCommand {

    /**
     * Flag indicating whether this command is the last which was executed
     * before saving.
     */
    private boolean lastSaved = false;

    /** Instance of the anime before it was edited. */
    protected Anime oldAnime = null;

    /** Instance containing the edited episodes. */
    protected Anime newAnime = null;

    /** Instance of the app. */
    protected Manami app = null;


    @Override
    public boolean execute() {
        app.removeAnime(oldAnime.getId());
        copyNullTarget(oldAnime, newAnime);
        return app.addAnime(newAnime);
    }


    @Override
    public void undo() {
        app.removeAnime(newAnime.getId());
        app.addAnime(oldAnime);
    }


    @Override
    public void redo() {
        execute();
    }


    @Override
    public boolean isLastSaved() {
        return lastSaved;
    }


    @Override
    public void setLastSaved(final boolean value) {
        lastSaved = value;
    }
}

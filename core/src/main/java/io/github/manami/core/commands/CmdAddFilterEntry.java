package io.github.manami.core.commands;

import io.github.manami.core.Manami;
import io.github.manami.dto.entities.FilterEntry;

/**
 * @author manami-project
 * @since 2.7.0
 */
public class CmdAddFilterEntry extends AbstractReversibleCommand {

    private final Manami app;
    private final FilterEntry entry;


    /**
     * Constructor
     *
     * @since 2.7.0
     * @param entry Anime that is being added.
     * @param application Instance of the application which reveals access to the persistence functionality.
     */
    public CmdAddFilterEntry(final FilterEntry entry, final Manami application) {
        this.entry = entry;
        app = application;
    }


    @Override
    public boolean execute() {
        return app.filterAnime(entry);
    }


    @Override
    public void undo() {
        app.removeFromFilterList(entry.getInfoLink());
    }
}

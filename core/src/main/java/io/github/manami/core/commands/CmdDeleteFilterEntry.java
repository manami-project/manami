package io.github.manami.core.commands;

import io.github.manami.core.Manami;
import io.github.manami.dto.entities.FilterEntry;

/**
 * @author manami project
 * @since 2.7.0
 */
public class CmdDeleteFilterEntry extends AbstractReversibleCommand {

    private final FilterEntry entry;


    /**
     * Constructor
     *
     * @since 2.7.0
     * @param entry {@link FilterEntry} that is supposed to be deleted.
     * @param application Instance of the application which reveals access to the persistence functionality.
     */
    public CmdDeleteFilterEntry(final FilterEntry entry, final Manami application) {
        this.entry = entry;
        app = application;
    }


    @Override
    public boolean execute() {
        return app.removeFromFilterList(entry.getInfoLink());
    }


    @Override
    public void undo() {
        app.filterAnime(entry);
    }
}

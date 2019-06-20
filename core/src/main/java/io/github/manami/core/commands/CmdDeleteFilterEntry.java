package io.github.manami.core.commands;

import io.github.manami.core.Manami;
import io.github.manami.dto.entities.FilterListEntry;


public class CmdDeleteFilterEntry extends AbstractReversibleCommand {

    private final FilterListEntry entry;


    /**
     * @param entry {@link FilterListEntry} that is supposed to be deleted.
     * @param application Instance of the application which reveals access to the persistence functionality.
     */
    public CmdDeleteFilterEntry(final FilterListEntry entry, final Manami application) {
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

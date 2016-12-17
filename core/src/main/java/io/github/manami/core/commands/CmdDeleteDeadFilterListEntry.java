package io.github.manami.core.commands;

import io.github.manami.core.Manami;
import io.github.manami.dto.entities.FilterEntry;

public class CmdDeleteDeadFilterListEntry extends AbstractReversibleCommand {

    private final FilterEntry entry;


    public CmdDeleteDeadFilterListEntry(final FilterEntry entry, final Manami application) {
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

package io.github.manami.core.commands;

import io.github.manami.core.Manami;
import io.github.manami.dto.entities.FilterListEntry;

public class CmdDeleteDeadFilterListEntry extends AbstractReversibleCommand {

    private final FilterListEntry entry;


    public CmdDeleteDeadFilterListEntry(final FilterListEntry entry, final Manami application) {
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

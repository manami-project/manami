package io.github.manami.core.commands;

import io.github.manami.core.Manami;
import io.github.manami.dto.entities.WatchListEntry;

public class CmdDeleteDeadWatchListEntry extends AbstractReversibleCommand {

    private final WatchListEntry entry;


    public CmdDeleteDeadWatchListEntry(final WatchListEntry entry, final Manami application) {
        this.entry = entry;
        app = application;
    }


    @Override
    public boolean execute() {
        return app.removeFromWatchList(entry.getInfoLink());
    }


    @Override
    public void undo() {
        app.watchAnime(entry);
    }
}

package io.github.manami.core.services.events;

import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdChangeLocation;
import io.github.manami.core.commands.ReversibleCommand;
import io.github.manami.dto.entities.Anime;

/**
 * @author manami-project
 * @since 2.10.0
 */
public class RelativizeLocationEvent extends AbstractEvent implements ReversibleCommandEvent {

    private final CmdChangeLocation cmd;


    public RelativizeLocationEvent(final Anime anime, final String newValue, final Manami app) {
        super(anime);
        cmd = new CmdChangeLocation(anime, newValue, app);
    }


    @Override
    public ReversibleCommand getCommand() {
        return cmd;
    }
}

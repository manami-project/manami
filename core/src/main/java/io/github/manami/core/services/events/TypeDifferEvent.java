package io.github.manami.core.services.events;

import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdChangeType;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;

/**
 * @author manami-project
 * @since 2.6.0
 */
public class TypeDifferEvent extends AbstractEvent implements ReversibleCommandEvent {

    private final CmdChangeType command;


    public TypeDifferEvent(final Anime anime, final AnimeType newValue, final Manami app) {
        super(anime);
        command = new CmdChangeType(anime, newValue, app);
    }


    /**
     * @return the command
     */
    @Override
    public CmdChangeType getCommand() {
        return command;
    }
}

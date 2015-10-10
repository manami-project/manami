package io.github.manami.core.services.events;

import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdChangeEpisodes;
import io.github.manami.core.commands.ReversibleCommand;
import io.github.manami.dto.entities.Anime;

/**
 * @author manami-project
 * @since 2.6.0
 */
public class EpisodesDifferEvent extends AbstractEvent implements ReversibleCommandEvent {

    private final CmdChangeEpisodes cmd;


    public EpisodesDifferEvent(final Anime anime, final int newValue, final Manami app) {
        super(anime);
        cmd = new CmdChangeEpisodes(anime, newValue, app);
    }


    @Override
    public ReversibleCommand getCommand() {
        return cmd;
    }
}

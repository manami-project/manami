package io.github.manami.core.services.events;

import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdChangeTitle;
import io.github.manami.dto.entities.Anime;
import lombok.Getter;

/**
 * @author manami-project
 * @since 2.6.0
 */
public class TitleDifferEvent extends AbstractEvent implements ReversibleCommandEvent {

    @Getter
    private final CmdChangeTitle command;


    public TitleDifferEvent(final Anime anime, final String newValue, final Manami app) {
        super(anime);
        command = new CmdChangeTitle(anime, newValue, app);
    }
}

package io.github.manami.core.services.events;

import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdChangeType;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import lombok.Getter;

public class TypeDifferEvent extends AbstractEvent implements ReversibleCommandEvent {

    @Getter
    private final CmdChangeType command;


    public TypeDifferEvent(final Anime anime, final AnimeType newValue, final Manami app) {
        super(anime);
        command = new CmdChangeType(anime, newValue, app);
    }
}

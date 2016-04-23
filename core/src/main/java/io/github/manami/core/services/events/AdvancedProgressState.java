package io.github.manami.core.services.events;

import io.github.manami.dto.entities.Anime;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains the state as well as payload.
 *
 * @author manami-project
 * @since 2.4.0
 */
public class AdvancedProgressState extends ProgressState {

    @Getter
    @Setter
    private Anime anime;


    public AdvancedProgressState(final int done, final int todo, final Anime anime) {
        super(done, todo);
        this.anime = anime;
    }
}

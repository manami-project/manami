package io.github.manami.core.services.events;

import io.github.manami.dto.entities.Anime;

/**
 * Contains the state as well as payload.
 *
 * @author manami-project
 * @since 2.4.0
 */
public class AdvancedProgressState extends ProgressState {

    private Anime anime;


    public AdvancedProgressState(final int done, final int todo, final Anime anime) {
        super(done, todo);
        this.anime = anime;
    }


    /**
     * @return the anime
     */
    public Anime getAnime() {
        return anime;
    }


    /**
     * @param anime
     *            the anime to set
     */
    public void setAnime(final Anime anime) {
        this.anime = anime;
    }
}

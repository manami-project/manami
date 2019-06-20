package io.github.manami.core.services.events;

import lombok.Getter;

/**
 * Used by services to notify observers of the progress.
 */
public class ProgressState {

    /** Number of items which have already been processed. */
    @Getter
    private final int done;

    /** Number of items which still need to be processed. */
    @Getter
    private final int todo;


    /**
     * @param done Number of processed items.
     * @param todo Number of items which still need to be processed.
     */
    public ProgressState(final int done, final int todo) {
        this.done = done;
        this.todo = todo;
    }
}

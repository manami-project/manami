package io.github.manami.core.services.events;

import io.github.manami.core.commands.ReversibleCommand;

/**
 * @author manami-project
 *
 */
public interface ReversibleCommandEvent {

    ReversibleCommand getCommand();
}

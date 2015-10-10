/**
 *
 */
package io.github.manami.core.services.events;

import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdChangeTitle;
import io.github.manami.core.commands.ReversibleCommand;
import io.github.manami.dto.entities.Anime;

/**
 * @author manami-project
 * @since 2.6.0
 */
public class TitleDifferEvent extends AbstractEvent implements ReversibleCommandEvent {

    private final CmdChangeTitle cmd;


    public TitleDifferEvent(final Anime anime, final String newValue, final Manami app) {
        super(anime);
        cmd = new CmdChangeTitle(anime, newValue, app);
    }


    @Override
    public ReversibleCommand getCommand() {
        return cmd;
    }
}

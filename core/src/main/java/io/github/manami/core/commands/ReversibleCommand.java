package io.github.manami.core.commands;

/**
 * Interface for reversible commands.
 *
 * @author manami-project
 * @since 2.0.0
 */
public interface ReversibleCommand extends Command {

    /**
     * Undoes the last reversible command.
     *
     * @since 2.0.0
     */
    void undo();


    /**
     * Redoes the last reversible command which was undone before.
     *
     * @since 2.0.0
     */
    void redo();


    /**
     * True if this command is the last which was executed before saving.
     *
     * @since 2.0.0
     * @return True if this command is the last which was executed before
     *         saving.
     */
    boolean isLastSaved();


    /**
     * Set it to true if this command is the last which was executed before
     * saving.
     *
     * @since 2.0.0
     * @param value
     *            True if this command is the last which was executed before
     *            saving.
     */
    void setLastSaved(boolean value);
}

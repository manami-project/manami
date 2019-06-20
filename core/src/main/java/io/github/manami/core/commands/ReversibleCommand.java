package io.github.manami.core.commands;

/**
 * Interface for reversible commands.
 */
public interface ReversibleCommand extends Command {

    /**
     * Undoes the last reversible command.
     */
    void undo();


    /**
     * Redoes the last reversible command which was undone before.
     */
    void redo();


    /**
     * True if this command is the last which was executed before saving.
     * @return True if this command is the last which was executed before
     *         saving.
     */
    boolean isLastSaved();


    /**
     * Set it to true if this command is the last which was executed before
     * saving.
     * @param value True if this command is the last which was executed before saving.
     */
    void setLastSaved(boolean value);
}

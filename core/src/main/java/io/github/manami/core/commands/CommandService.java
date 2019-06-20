package io.github.manami.core.commands;

import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.eventbus.EventBus;

import io.github.manami.dto.events.AnimeListChangedEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * Command service keeps track of actions and is responsible for knowing if a
 * file is dirty or not.
 */
@Named
public class CommandService {

    /** Indicates whether the document is dirty or not. */
    @Getter
    @Setter
    private boolean isUnsaved = false;

    /** Stick with all undoable commands which have been made. */
    private Stack<ReversibleCommand> done = null;

    /** Stack with all commands which were made undone. */
    private Stack<ReversibleCommand> undone = null;

    /** EventBus to inform GUI. */
    private final EventBus eventBus;


    @Inject
    public CommandService(final EventBus eventBus) {
        this.eventBus = eventBus;
        done = new Stack<>();
        undone = new Stack<>();
    }


    /**
     * Executes a specific command.
     *
     * @param command {@link Command} to execute.
     */
    public void executeCommand(final ReversibleCommand command) {
        if (command.execute()) {
            done.add(command);
            setUnsaved(true);
            eventBus.post(new AnimeListChangedEvent());
        }
    }


    /**
     * Undoes the last reversible action.
     */
    public void undo() {
        if (!done.empty()) {
            final ReversibleCommand cmd = done.pop();
            undone.add(cmd);
            checkDirtyFlag();
            cmd.undo();
        }
    }


    /**
     * Redoes the last reversible action.
     */
    public void redo() {
        if (!undone.empty()) {
            final ReversibleCommand cmd = undone.pop();
            done.add(cmd);
            checkDirtyFlag();
            cmd.redo();
        }
    }


    /**
     * Check if the last executed command was the last one before saving.
     */
    private void checkDirtyFlag() {
        isUnsaved = !(done.empty() || (!done.empty() && done.peek().isLastSaved()));
    }


    /**
     * Clears the stack of done and undone commands.
     */
    public void clearAll() {
        done.clear();
        undone.clear();
        isUnsaved = false;
    }


    /**
     * Sets the last executed command anew.
     */
    public void resetDirtyFlag() {
        for (final ReversibleCommand cmd : done) {
            cmd.setLastSaved(false);
        }

        for (final ReversibleCommand cmd : undone) {
            cmd.setLastSaved(false);
        }

        if (done.size() > 0) {
            done.peek().setLastSaved(true);
        }
    }


    /**
     * Checks whether the stack for executed commands is empty or not.
     * @return True if no {@link Command} has been executed.
     */
    public boolean isEmptyDoneCommands() {
        return done.isEmpty();
    }


    /**
     * Checks whether the stack for undone commands is empty or not.
     * @return True if no {@link Command} has been made undone.
     */
    public boolean isEmptyUndoneCommands() {
        return undone.isEmpty();
    }
}

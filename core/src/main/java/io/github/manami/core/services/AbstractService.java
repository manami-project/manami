package io.github.manami.core.services;

import java.util.Observable;

import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 * @author manami project
 * @since 2.5.0
 */
public abstract class AbstractService<E> extends Observable implements BackgroundService {

    protected Service<E> service;
    private boolean interrupt = false;


    @Override
    public void cancel() {
        if (service != null && service.isRunning()) {
            service.cancel();
        }
    }


    @Override
    public boolean isRunning() {
        if (service == null) {
            return false;
        }

        return service.isRunning();
    }


    /**
     * @since 2.5.0
     * @return the failureEvent
     */
    protected EventHandler<WorkerStateEvent> getSuccessEvent() {
        return event -> {
            interrupt = true;
            setChanged();
            notifyObservers(Boolean.FALSE);
        };
    }


    /**
     * @since 2.5.0
     * @return the failureEvent
     */
    protected EventHandler<WorkerStateEvent> getFailureEvent() {
        return event -> {
            interrupt = true;
            setChanged();
            notifyObservers(Boolean.FALSE);
        };
    }


    @Override
    public void reset() {
        interrupt = false;
    }


    /**
     * @since 2.5.1
     * @return the run
     */
    public boolean isInterrupt() {
        return interrupt;
    }
}

package io.github.manami.core.services;

import java.util.Observable;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;


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


    protected EventHandler<WorkerStateEvent> getSuccessEvent() {
        return event -> {
            interrupt = true;
            setChanged();
            notifyObservers(Boolean.FALSE);
        };
    }


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


    public boolean isInterrupt() {
        return interrupt;
    }


    @Override
    public void start() {
        reset();

        service = new Service<E>() {

            @Override
            protected Task<E> createTask() {
                return new Task<E>() {

                    @Override
                    protected E call() throws Exception {
                        return execute();
                    }
                };
            }
        };

        service.setOnCancelled(getFailureEvent());
        service.setOnFailed(getFailureEvent());
        service.setOnSucceeded(getSuccessEvent());
        service.start();
    }


    abstract protected E execute();
}

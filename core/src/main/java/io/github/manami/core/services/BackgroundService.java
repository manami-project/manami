package io.github.manami.core.services;

import java.util.Observer;

/**
 * @author manami-project
 * @since 2.5.0
 */
public interface BackgroundService {

    void start();


    void cancel();


    /**
     * Checks whether the service is running or not.
     *
     * @return True if the service is running.
     */
    boolean isRunning();


    void reset();


    void addObserver(Observer observer);
}

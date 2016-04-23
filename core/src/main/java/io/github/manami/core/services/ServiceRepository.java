package io.github.manami.core.services;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Observable;
import java.util.Observer;

import javax.inject.Named;

import com.sun.javafx.collections.ObservableListWrapper;

import javafx.collections.ObservableList;

/**
 * @author manami-project
 * @since 2.5.0
 */
@Named
public class ServiceRepository implements Observer {

    private final ObservableList<BackgroundService> runningServices = new ObservableListWrapper<>(newArrayList());


    public void startService(final BackgroundService service) {
        if (service == null) {
            return;
        }

        synchronized (runningServices) {
            if (!runningServices.isEmpty()) {
                for (final BackgroundService curService : runningServices) {
                    final boolean isAlreadyRunning = !(service instanceof RelatedAnimeFinderService) && service.getClass().equals(curService.getClass());

                    if (isAlreadyRunning) {
                        curService.cancel();
                    }
                }
            }

            safelyStartService(service);
        }
    }


    /**
     * @since 2.7.2
     */
    public void cancelAllServices() {
        synchronized (runningServices) {
            while (!runningServices.isEmpty()) {
                runningServices.get(0).cancel();
            }
        }
    }


    /**
     * @since 2.5.0
     * @param service
     */
    private void safelyStartService(final BackgroundService service) {
        service.addObserver(this);
        runningServices.add(service);
        service.start();
    }


    @Override
    public void update(final Observable observable, final Object obj) {
        // we only want to know if a service succeeded or failed
        if (obj != null && observable != null && obj instanceof Boolean) {
            synchronized (runningServices) {
                runningServices.remove(observable);
            }
        }
    }
}

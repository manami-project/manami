package io.github.manami.core.services;

import java.util.Observable;
import java.util.Observer;

import javafx.collections.FXCollections;
import javax.inject.Named;


import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

/**
 * @author manami-project
 * @since 2.5.0
 */
@Named
@Slf4j
public class ServiceRepository implements Observer {

    private final ObservableList<BackgroundService> runningServices = FXCollections.observableArrayList();


    public void startService(final BackgroundService service) {
        if (service == null) {
            return;
        }

        synchronized (runningServices) {
            if (!runningServices.isEmpty()) {
                for (int index = 0; index < runningServices.size(); index++) {
                    final BackgroundService curService = runningServices.get(index);

                    final boolean isAlreadyRunning = !(service instanceof RelatedAnimeFinderService) && service.getClass().equals(curService.getClass());

                    if (isAlreadyRunning) {
                        curService.cancel();
                    }

                    final boolean isManuallyStartedService = runningServices.size() >= 3;

                    if (curService instanceof CacheInitializationService && isManuallyStartedService) {
                        log.info("Stopping cache init service.");
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

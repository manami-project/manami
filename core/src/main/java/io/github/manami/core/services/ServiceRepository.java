package io.github.manami.core.services;

import java.util.Observable;
import java.util.Observer;

import javafx.collections.FXCollections;
import javax.inject.Named;


import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

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
                }
            }

            safelyStartService(service);
        }
    }


    public void cancelAllServices() {
        synchronized (runningServices) {
            while (!runningServices.isEmpty()) {
                runningServices.get(0).cancel();
            }
        }
    }


    private void safelyStartService(final BackgroundService service) {
        service.addObserver(this);
        runningServices.add(service);
        service.start();
    }


    @Override
    public void update(final Observable observable, final Object obj) {
        // we only want to know if a service succeeded or failed
        if (observable != null && obj instanceof Boolean) {
            synchronized (runningServices) {
                runningServices.remove(observable);
            }
        }
    }
}

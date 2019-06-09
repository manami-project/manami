package io.github.manami.gui.utility;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public abstract class ReadOnlyObservableValue<T> implements ObservableValue<T> {

    @Override
    public void addListener(ChangeListener<? super T> listener) {
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
    }

    @Override
    public abstract T getValue();

    @Override
    public void addListener(InvalidationListener listener) {
    }

    @Override
    public void removeListener(InvalidationListener listener) {
    }
}

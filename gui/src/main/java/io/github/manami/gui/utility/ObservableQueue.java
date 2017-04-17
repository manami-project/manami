package io.github.manami.gui.utility;

import static com.google.common.collect.Lists.newLinkedList;

import java.util.Iterator;
import java.util.Queue;

import javafx.collections.ObservableListBase;

public class ObservableQueue<E> extends ObservableListBase<E> implements Queue<E> {

    private Queue<E> queue;


    public ObservableQueue(final Queue<E> queue) {
        this.queue = queue;
    }


    public ObservableQueue() {
        this(newLinkedList());
    }


    @Override
    public E element() {
        return queue.element();
    }


    @Override
    public boolean offer(final E e) {
        beginChange();

        final boolean result = queue.offer(e);

        if (result) {
            nextAdd(queue.size() - 1, queue.size());
        }

        endChange();

        return result;
    }


    @Override
    public E peek() {
        return queue.peek();
    }


    @Override
    public E poll() {
        beginChange();

        final E e = queue.poll();

        if (e != null) {
            nextRemove(0, e);
        }

        endChange();

        return e;
    }


    @Override
    public E remove() {
        beginChange();

        try {
            final E e = queue.remove();
            nextRemove(0, e);
            return e;
        } finally {
            endChange();
        }
    }


    @Override
    public E get(final int index) {
        final Iterator<E> iterator = queue.iterator();

        for (int i = 0; i < index; i++) {
            iterator.next();
        }

        return iterator.next();
    }


    @Override
    public int size() {
        return queue.size();
    }


    @Override
    public void clear() {
        while (queue.peek() != null) {
            queue.poll();
        }
    }
}

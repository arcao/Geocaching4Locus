package com.arcao.geocaching4locus.base.util;

import android.support.annotation.NonNull;

import java.util.Iterator;
import java.util.List;

public class ReverseListIterator<T> implements Iterator<T>, Iterable<T> {
    private final List<T> list;
    private int position;

    public ReverseListIterator(List<T> list) {
        this.list = list;
        this.position = list.size() - 1;
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return position >= 0;
    }

    @Override
    public T next() {
        //noinspection ValueOfIncrementOrDecrementUsed
        return list.get(position--);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

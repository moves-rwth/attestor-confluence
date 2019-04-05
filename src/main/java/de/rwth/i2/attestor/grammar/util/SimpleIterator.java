package de.rwth.i2.attestor.grammar.util;

import java.util.Iterator;

/**
 * A shortcut iterator that only requries a single method to be implemented. Cannot return null.
 */
public abstract class SimpleIterator<T> implements Iterator<T> {
    private T nextElement;

    public abstract T computeNext();

    public SimpleIterator() {
        nextElement = computeNext();
    }

    @Override
    public boolean hasNext() {
        return nextElement != null;
    }

    @Override
    public T next() {
        T temp = nextElement;
        nextElement = computeNext();
        return temp;
    }
}

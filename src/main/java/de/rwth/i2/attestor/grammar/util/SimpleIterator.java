package de.rwth.i2.attestor.grammar.util;

import java.util.Iterator;

/**
 * A shortcut iterator that only requries a single method to be implemented. Cannot return null.
 */
public abstract class SimpleIterator<T> implements Iterator<T> {
    private T nextElement = null;
    boolean firstCall = true;

    public abstract T computeNext();

    private void checkFirstCall() {
        if (firstCall) {
            nextElement = computeNext();
            firstCall = false;
        }
    }

    @Override
    public boolean hasNext() {
        checkFirstCall();
        return nextElement != null;
    }

    @Override
    public T next() {
        checkFirstCall();
        T temp = nextElement;
        nextElement = computeNext();
        return temp;
    }
}

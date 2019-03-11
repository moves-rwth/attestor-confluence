package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.util.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * An overlapping describes how two graphs (HC1 & HC2) overlap to build a new graph H.
 * It can be directly classified by the nodes of HC1 and HC2 which map to the same node in H. These nodes can be
 * understood as node equivalences. Every overlapping therefore corresponds to a specific subset of the product of
 * the node sets of HC1 and HC2.
 */
public abstract class Overlapping<Element extends GraphElement> implements Iterable<Overlapping<Element>> {
    /**
     * TODO:
     * The attributes hc1Remaining, hc2Remaining might better use a different data structures.
     * Instead of a TreeSet hc2Remaining might better use two hash maps that points to the successor / predecessor (and to save the smallest element)  --> Constant lookup time instead of O(log(n))
     * And hc1Remaining might better use an ArrayList.
     *
     * TODO:
     * Don't copy every information into the new object, but only add the new information. Save a pointer to the previous
     * element.
     * Advantage: Faster object creation
     * Drawback: Slower queries on object
     */
    private final TreeSet<Element> hc1Remaining, hc2Remaining;
    private final Pair<Element, Element> lastAddedEquivalence;
    private final Map<Element, Element> mapHC1toHC2, mapHC2toHC1;
    private final HeapConfigurationContext context;

    Overlapping(HeapConfigurationContext context, Collection<Element> hc1Remaining,
                          Collection<Element> hc2Remaining, Map<Element, Element> mapHC1toHC2,
                          Map<Element, Element> mapHC2toHC1) {
        this.context = context;
        this.hc1Remaining = new TreeSet<>(hc1Remaining);
        this.hc2Remaining = new TreeSet<>(hc2Remaining);
        this.mapHC1toHC2 = mapHC1toHC2;
        this.mapHC2toHC1 = mapHC2toHC1;
        this.lastAddedEquivalence = null;
    }

    /**
     * Initializes an overlapping where all Elements are disjoint
     */
    Overlapping(HeapConfigurationContext context, Collection<Element> hc1, Collection<Element> hc2) {
        this.context = context;
        hc1Remaining = new TreeSet<>(hc1);
        hc2Remaining = new TreeSet<>(hc2);
        lastAddedEquivalence = null;
        mapHC1toHC2 =  new HashMap<>();
        mapHC2toHC1 =  new HashMap<>();
    }


    Overlapping(Overlapping<Element> oldOverlapping, Pair<Element, Element> newEquivalence) {
        context = oldOverlapping.context;
        hc1Remaining = new TreeSet<>(oldOverlapping.hc1Remaining);
        hc1Remaining.remove(newEquivalence.first());
        hc2Remaining = new TreeSet<>(oldOverlapping.hc2Remaining);
        hc2Remaining.remove(newEquivalence.second());
        lastAddedEquivalence = newEquivalence;
        mapHC1toHC2 =  new HashMap<>(oldOverlapping.mapHC1toHC2);
        mapHC1toHC2.put(newEquivalence.first(), newEquivalence.second());
        mapHC2toHC1 =  new HashMap<>(oldOverlapping.mapHC2toHC1);
        mapHC2toHC1.put(newEquivalence.second(), newEquivalence.first());
    }

    /**
     * Returns the next possible successor for a node equivalence.
     *
     * @param previousPair  The last equivalence returned from this method. Set to null for the first call.
     * @return The returned pair corresponds to the immediate successor child after the 'previousPair'.
     *         If there are no more successors this method return null.
     */
    Pair<Element, Element> getNextEquivalence(Pair<Element, Element> previousPair) {
        if (hc1Remaining.isEmpty() || hc2Remaining.isEmpty()) {
            // No overlapping possible
            return null;
        }
        Element hc1Old, hc2Old;
        if (previousPair == null) {
            if (lastAddedEquivalence == null) {
                // This is the base overlapping
                return new Pair<>(hc1Remaining.first(), hc2Remaining.first());
            } else {
                // This is not the base overlapping -> The last added equivalence is the actual oldPair
                hc1Old = hc1Remaining.higher(lastAddedEquivalence.first());
                if (hc1Old == null) {
                    return null;
                }
                hc2Old = hc2Remaining.first();
                return new Pair<>(hc1Old, hc2Old);
            }
        } else {
            hc1Old = previousPair.first();
            hc2Old = previousPair.second();
        }

        Element hc1New, hc2New;
        hc2New = hc2Remaining.higher(hc2Old);
        if (hc2New == null) {
            // If there is no higher node in hc2 the next equivalence we look for a higher node in hc1
            hc1New = hc1Remaining.higher(hc1Old);
            if (hc1New == null) {
                // There are no more valid Element in hc1
                return null;
            }
            // Start again by the lowest available node in hc2
            hc2New = hc2Remaining.first(); // hc2Remaining is not empty (checked at the beginning)
        } else {
            hc1New = hc1Old;
        }
        return new Pair<>(hc1New, hc2New);
    }

    TreeSet<Element> getHc1Remaining() {
        return hc1Remaining;
    }

    TreeSet<Element> getHc2Remaining() {
        return hc2Remaining;
    }

    /**
     * Returns the overlapping that contains only a single equivalence more.
     * Furthermore the added equivalence has to come after the 'lastAddedEquivalence' from this object
     * according to the canonical ordering of node equivalences.
     * The isNextPairCompatible method is used to only include compatible Element
     *
     * @deprecated This should be done by the iterator
     */
    @Deprecated
    Collection<Overlapping<Element>> getAllNextOverlappings() {
        Pair<Element, Element> nextNodeEquivalence;
        nextNodeEquivalence = getNextEquivalence(lastAddedEquivalence);
        Collection<Overlapping<Element>> result = new ArrayList<>();
        while (nextNodeEquivalence != null) {
            if (this.isNextPairCompatible(nextNodeEquivalence)) {
                result.add(getOverlapping(nextNodeEquivalence));
            }
            nextNodeEquivalence = getNextEquivalence(nextNodeEquivalence);
        }
        return result;
    }

    public HeapConfigurationContext getContext() {
        return context;
    }

    @Override
    public Iterator<Overlapping<Element>> iterator() {
        return new OverlappingIterator<>(this);
    }

    Map<Element, Element> getMapHC1toHC2() {
        return new HashMap<>(mapHC1toHC2); // TODO: Maybe don't copy the map here
    }

    Map<Element, Element> getMapHC2toHC1() {
        return new HashMap<>(mapHC2toHC1);  // TODO: Maybe don't copy the map here
    }

    /**
     * Checks if there are any elements in the intersection
     *
     * @return true if there are no elements in the intersection
     */
    public boolean isEmpty() {
        return mapHC1toHC2.isEmpty();
    }

    /**
     *
     *
     * @param newPair  A pair of node equivalences
     * @return true if 'newPair' can be added to this Overlapping
     */
    abstract boolean isNextPairCompatible(Pair<Element, Element> newPair);

    /**
     * Returns the Overlapping if the newPair is added to the equivalences of this object.
     */
    abstract Overlapping<Element> getOverlapping(Pair<Element, Element> newPair);

}

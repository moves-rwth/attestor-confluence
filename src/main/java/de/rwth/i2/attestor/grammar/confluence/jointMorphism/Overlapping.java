package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.util.Pair;

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
     * If there is no successor returns null
     */
    private Pair<Element, Element> getNextEquivalence(Pair<Element, Element> oldPair) {
        Element hc1New, hc2New;
        hc2New = hc2Remaining.higher(oldPair.second());
        if (hc2New == null) {
            // If there is no higher node in hc2 the next equivalence we look for a higher node in hc1
            hc1New = hc1Remaining.higher(oldPair.first());
            if (hc1New == null) {
                // There are no more valid Element in hc1
                return null;
            }
            // Start again by the lowest available node in hc2
            hc2New = hc2Remaining.first();
            if (hc2New == null) {
                // There are no more valid Elements in hc2
                return null;
            }
        } else {
            hc1New = oldPair.first();
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
     */
    Collection<Overlapping<Element>> getAllNextOverlappings() {
        Pair<Element, Element> nextNodeEquivalence;
        if (lastAddedEquivalence == null) {
            if (hc1Remaining.isEmpty() || hc2Remaining.isEmpty()) {
                // No overlapping possible
                return new ArrayList<>();
            }
            nextNodeEquivalence = new Pair<>(hc1Remaining.first(), hc2Remaining.first());
        } else {
            nextNodeEquivalence = getNextEquivalence(lastAddedEquivalence);
        }
        Collection<Overlapping<Element>> result = new ArrayList<>();
        while (nextNodeEquivalence != null) {
            if (this.isNextPairCompatible(nextNodeEquivalence)) {
                result.add(getOverlapping(nextNodeEquivalence));
            }
            nextNodeEquivalence = getNextEquivalence(nextNodeEquivalence);
        }
        return result;
    }

    HeapConfigurationContext getContext() {
        return context;
    }

    @Override
    public Iterator<Overlapping<Element>> iterator() {
        return new OverlappingIterator<>(this);
    }

    Map<Element, Element> getMapHC1toHC2() {
        return new HashMap<>(mapHC1toHC2); // TODO: Maybe don't copy the map here
    }

    Element getHC2Element(Element hc1Element) {
        return mapHC1toHC2.getOrDefault(hc1Element, null);
    }

    Map<Element, Element> getMapHC2toHC1() {
        return new HashMap<>(mapHC2toHC1);  // TODO: Maybe don't copy the map here
    }

    Element getHC1Element(Element hc2Element) {
        return mapHC2toHC1.getOrDefault(hc2Element, null);
    }

    Pair<Element, Element> getLastAddedEquivalence() {
        return lastAddedEquivalence;
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

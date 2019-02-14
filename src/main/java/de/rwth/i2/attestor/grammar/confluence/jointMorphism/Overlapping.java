package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.util.Pair;

import java.util.*;

/**
 * An overlapping describes how two graphs (HC1 & HC2) overlap to build a new graph H.
 * It can be directly classified by the nodes of HC1 and HC2 which map to the same node in H. These nodes can be
 * understood as node equivalences. Every overlapping therefore corresponds to a specific subset of the product of
 * the node sets of HC1 and HC2.
 */
public abstract class Overlapping implements Iterable<Overlapping> {
    /**
     * TODO:
     * The attributes hc1Remaining, hc2Remaining might better use a different datastructures.
     * Instead of a TreeSet hc2Remaining might better use two hashmaps that points to the successor / predecessor (and to save the smallest element)  --> Constant lookup time instead of O(log(n))
     * And hc1Remaining might better use an ArrayList.
     *
     * TODO:
     * Don't copy every information into the new object, but only add the new information. Save a pointer to the previous
     * element.
     * Advantage: Faster object creation
     * Drawback: Slower queries on object
     */
    private final TreeSet<GraphElement> hc1Remaining, hc2Remaining;
    private final Pair<GraphElement, GraphElement> lastAddedEquivalence;
    private final Map<GraphElement, GraphElement> mapHC1toHC2, mapHC2toHC1;
    private final HeapConfigurationContext context;

    protected Overlapping(HeapConfigurationContext context, Collection<GraphElement> hc1Remaining,
                          Collection<GraphElement> hc2Remaining, Map<GraphElement, GraphElement> mapHC1toHC2,
                          Map<GraphElement, GraphElement> mapHC2toHC1) {
        this.context = context;
        this.hc1Remaining = new TreeSet<>(hc1Remaining);
        this.hc2Remaining = new TreeSet<>(hc2Remaining);
        this.mapHC1toHC2 = mapHC1toHC2;
        this.mapHC2toHC1 = mapHC2toHC1;
        this.lastAddedEquivalence = null;
    }

    /**
     * Initializes an overlapping where all GraphElements are disjoint
     */
    protected Overlapping(HeapConfigurationContext context, Collection<GraphElement> hc1, Collection<GraphElement> hc2) {
        this.context = context;
        hc1Remaining = new TreeSet<>(hc1);
        hc2Remaining = new TreeSet<>(hc2);
        lastAddedEquivalence = null;
        mapHC1toHC2 =  new HashMap<>();
        mapHC2toHC1 =  new HashMap<>();
    }


    protected Overlapping(Overlapping oldOverlapping, Pair<GraphElement, GraphElement> newEquivalence) {
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
    private Pair<GraphElement, GraphElement> getNextEquivalence(Pair<GraphElement, GraphElement> oldPair) {
        GraphElement hc1New, hc2New;
        hc2New = hc2Remaining.higher(oldPair.second());
        if (hc2New == null) {
            // If there is no higher node in hc2 the next equivalence we look for a higher node in hc1
            hc1New = hc1Remaining.higher(oldPair.first());
            if (hc1New == null) {
                // There are no more valid GraphElements in hc1
                return null;
            }
            // Start again by the lowest available node in hc2
            hc2New = hc2Remaining.first();
            if (hc2New == null) {
                // There are no more valid GraphElements in hc2
                return null;
            }
        } else {
            hc1New = oldPair.first();
        }
        return new Pair<>(hc1New, hc2New);
    }

    public TreeSet<GraphElement> getHc1Remaining() {
        return hc1Remaining;
    }

    public TreeSet<GraphElement> getHc2Remaining() {
        return hc2Remaining;
    }

    /**
     * Returns the overlapping that contains only a single equivalence more.
     * Furthermore the added equivalence has to come after the 'lastAddedEquivalence' from this object
     * according to the canonical ordering of node equivalences.
     * The isNextPairCompatible method is used to only include compatible GraphElement
     */
    protected Collection<Overlapping> getAllNextEquivalences() {
        Pair<GraphElement, GraphElement> nextNodeEquivalence;
        if (lastAddedEquivalence == null) {
            if (hc1Remaining.isEmpty() || hc2Remaining.isEmpty()) {
                throw new RuntimeException("First overlapping cannot be obtained.");
            }
            nextNodeEquivalence = new Pair<>(hc1Remaining.first(), hc2Remaining.first());
        } else {
            nextNodeEquivalence = getNextEquivalence(lastAddedEquivalence);
        }
        Collection<Overlapping> result = new ArrayList<>();
        while (nextNodeEquivalence != null) {
            if (this.isNextPairCompatible(nextNodeEquivalence)) {
                result.add(getOverlapping(nextNodeEquivalence));
            }
            nextNodeEquivalence = getNextEquivalence(nextNodeEquivalence);
        }
        return result;
    }

    protected HeapConfigurationContext getContext() {
        return context;
    }

    @Override
    public Iterator<Overlapping> iterator() {
        return new OverlappingIterator(this);
    }

    public Map<GraphElement, GraphElement> getMapHC1toHC2() {
        return new HashMap<>(mapHC1toHC2); // TODO: Maybe don't copy the map here
    }

    public Map<GraphElement, GraphElement> getMapHC2toHC1() {
        return new HashMap<>(mapHC2toHC1);  // TODO: Maybe don't copy the map here
    }

    /**
     *
     *
     * @param newPair  A pair of node equivalences
     * @return true if 'newPair' can be added to this Overlapping
     */
    abstract boolean isNextPairCompatible(Pair<GraphElement, GraphElement> newPair);

    /**
     * Returns the Overlapping if the newPair is added to the equivalences of this object.
     * @param newPair
     * @return
     */
    abstract Overlapping getOverlapping(Pair<GraphElement, GraphElement> newPair);

}

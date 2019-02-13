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
     * The attributes l1Remaining, l2Remaining might better use a different datastructures.
     * Instead of a TreeSet l2Remaining might better use two hashmaps that points to the successor / predecessor (and to save the smallest element)  --> Constant lookup time instead of O(log(n))
     * And l1Remaining might better use an ArrayList.
     *
     * TODO:
     * Don't copy every information into the new object, but only add the new information. Save a pointer to the previous
     * element.
     * Advantage: Faster object creation
     * Drawback: Slower queries on object
     */
    private final TreeSet<GraphElement> l1Remaining, l2Remaining;
    private final Pair<GraphElement, GraphElement> lastAddedEquivalence;
    private final Map<GraphElement, GraphElement> mapL1toL2, mapL2toL1;
    private final HeapConfigurationContext context;

    protected Overlapping(HeapConfigurationContext context, Collection<GraphElement> l1Remaining,
                          Collection<GraphElement> l2Remaining, Map<GraphElement, GraphElement> mapL1toL2,
                          Map<GraphElement, GraphElement> mapL2toL1) {
        this.context = context;
        this.l1Remaining = new TreeSet<>(l1Remaining);
        this.l2Remaining = new TreeSet<>(l2Remaining);
        this.mapL1toL2 = mapL1toL2;
        this.mapL2toL1 = mapL2toL1;
        this.lastAddedEquivalence = null;
    }

    /**
     * Initializes an overlapping where all GraphElements are disjoint
     */
    protected Overlapping(HeapConfigurationContext context, Collection<GraphElement> l1, Collection<GraphElement> l2) {
        this.context = context;
        l1Remaining = new TreeSet<>(l1);
        l2Remaining = new TreeSet<>(l2);
        lastAddedEquivalence = null;
        mapL1toL2 =  new HashMap<>();
        mapL2toL1 =  new HashMap<>();
    }


    protected Overlapping(Overlapping oldOverlapping, Pair<GraphElement, GraphElement> newEquivalence) {
        context = oldOverlapping.context;
        l1Remaining = new TreeSet<>(oldOverlapping.l1Remaining);
        l1Remaining.remove(newEquivalence.first());
        l2Remaining = new TreeSet<>(oldOverlapping.l2Remaining);
        l2Remaining.remove(newEquivalence.second());
        lastAddedEquivalence = newEquivalence;
        mapL1toL2 =  new HashMap<>(oldOverlapping.mapL1toL2);
        mapL1toL2.put(newEquivalence.first(), newEquivalence.second());
        mapL2toL1 =  new HashMap<>(oldOverlapping.mapL2toL1);
        mapL2toL1.put(newEquivalence.second(), newEquivalence.first());
    }

    /**
     * Returns the next possible successor for a node equivalence.
     * If there is no successor returns null
     */
    private Pair<GraphElement, GraphElement> getNextEquivalence(Pair<GraphElement, GraphElement> oldPair) {
        GraphElement l1New, l2New;
        l2New = l2Remaining.higher(oldPair.second());
        if (l2New == null) {
            // If there is no higher node in hc2 the next equivalence we look for a higher node in hc1
            l1New = l1Remaining.higher(oldPair.first());
            if (l1New == null) {
                // There are no more valid GraphElements in hc1
                return null;
            }
            // Start again by the lowest available node in hc2
            l2New = l2Remaining.first();
            if (l2New == null) {
                // There are no more valid GraphElements in hc2
                return null;
            }
        } else {
            l1New = oldPair.first();
        }
        return new Pair<>(l1New, l2New);
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
            if (l1Remaining.isEmpty() || l2Remaining.isEmpty()) {
                throw new RuntimeException("First overlapping cannot be obtained.");
            }
            nextNodeEquivalence = new Pair<>(l1Remaining.first(), l2Remaining.first());
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

    public Map<GraphElement, GraphElement> getMapL1toL2() {
        return new HashMap<>(mapL1toL2); // TODO: Maybe don't copy the map here
    }

    public Map<GraphElement, GraphElement> getMapL2toL1() {
        return new HashMap<>(mapL2toL1);  // TODO: Maybe don't copy the map here
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

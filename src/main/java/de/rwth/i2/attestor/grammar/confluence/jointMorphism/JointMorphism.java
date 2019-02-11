package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

/**
 * A joint-morphism is a morphism from two HCs hc1 and hc2 to an image hc H.
 * It can be directly classified by the nodes of hc1 and hc2 which map to the same node in H. These nodes can be
 * understood as node equivalences. Every joint-morphism therefore corresponds to a specific subset of the product of
 * the node sets of hc1 and hc2.
 */
public abstract class JointMorphism implements Iterable<JointMorphism> {
    private final TreeSet<GraphElement> l1Remaining, l2Remaining;
    private final Pair<GraphElement, GraphElement> lastAddedEquivalence;
    private final Map<GraphElement, GraphElement> mapL1toL2, mapL2toL1;

    /**
     * Initializes jointMorphism where all nodes are disjoint
     */
    public JointMorphism(Collection<GraphElement> l1, Collection<GraphElement> l2) {
        l1Remaining = new TreeSet<>();
        for (GraphElement elem : l1) {
            l1Remaining.add(elem);
        }
        l2Remaining = new TreeSet<>();
        for (GraphElement elem : l2) {
            l2Remaining.add(elem);
        }
        lastAddedEquivalence = null;
        mapL1toL2 =  new HashMap<>();
        mapL2toL1 =  new HashMap<>();
    }

    protected JointMorphism(JointMorphism oldJointMorphism) {  // TODO: Check if we need this method
        l1Remaining = new TreeSet<>(oldJointMorphism.l1Remaining);
        l2Remaining = new TreeSet<>(oldJointMorphism.l2Remaining);
        lastAddedEquivalence = oldJointMorphism.lastAddedEquivalence;
        mapL1toL2 =  new HashMap<>(oldJointMorphism.mapL1toL2);
        mapL2toL1 =  new HashMap<>(oldJointMorphism.mapL2toL1);
    }

    protected JointMorphism(JointMorphism oldJointMorphism, Pair<GraphElement, GraphElement> newEquivalence) { // TODO: Check if we need this method
        l1Remaining = new TreeSet<>(oldJointMorphism.l1Remaining);
        l1Remaining.remove(newEquivalence.first());
        l2Remaining = new TreeSet<>(oldJointMorphism.l2Remaining);
        l2Remaining.remove(newEquivalence.second());
        lastAddedEquivalence = newEquivalence;
        mapL1toL2 =  new HashMap<>(oldJointMorphism.mapL1toL2);
        mapL1toL2.put(newEquivalence.first(), newEquivalence.second());
        mapL2toL1 =  new HashMap<>(oldJointMorphism.mapL2toL1);
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
                // There are no more valid nodes in hc1
                return null;
            }
            // Start again by the lowest available node in hc2
            l2New = l2Remaining.first();
            if (l2New == null) {
                // There are no more valid nodes in hc2
                return null;
            }
        } else {
            l1New = oldPair.first();
        }
        return new Pair<>(l1New, l2New);
    }

    /**
     * Returns the joint morphisms that contains only a single equivalence more.
     * Furthermore the added equivalence has to come after the 'lastAddedEquivalence' from this object
     * according to the canonical ordering of node equivalences.
     * The isNextPairCompatible method is used to only include compatible nodes
     */
    protected Collection<JointMorphism> getAllNextEquivalences() {
        Pair<GraphElement, GraphElement> nextNodeEquivalence;
        if (lastAddedEquivalence == null) {
            if (l1Remaining.isEmpty() || l2Remaining.isEmpty()) {
                throw new RuntimeException("First joint-morphism cannot be obtained.");
            }
            nextNodeEquivalence = new Pair<>(l1Remaining.first(), l2Remaining.first());
        } else {
            nextNodeEquivalence = getNextEquivalence(lastAddedEquivalence);
        }
        Collection<JointMorphism> result = new ArrayList<>();
        while (nextNodeEquivalence != null) {
            if (this.isNextPairCompatible(nextNodeEquivalence)) {
                result.add(getJointMorphism(nextNodeEquivalence));
            }
            nextNodeEquivalence = getNextEquivalence(nextNodeEquivalence);
        }
        return result;
    }

    @Override
    public Iterator<JointMorphism> iterator() {
        return new JointMorphismIterator(this);
    }

    /**
     *
     *
     * @param newPair  A pair of node equivalences
     * @return true if 'newPair' can be added to this JointMorphism
     */
    abstract boolean isNextPairCompatible(Pair<GraphElement, GraphElement> newPair);

    /**
     * Returns the
     * @param newPair
     * @return
     */
    abstract JointMorphism getJointMorphism(Pair<GraphElement, GraphElement> newPair);

}

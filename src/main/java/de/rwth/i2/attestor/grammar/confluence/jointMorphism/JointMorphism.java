package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
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
    /**
     * TODO:
     * The attributes l1Remaining, l2Remaining might better use a different datastructures.
     * Instead of a TreeSet l2Remaining might better use two hashmaps that points to the successor / predecessor (and to save the smallest element)  --> Constant lookup time instead of O(log(n))
     * And l1Remaining might better use an ArrayList.
     */
    private final TreeSet<GraphElement> l1Remaining, l2Remaining;
    private final Pair<GraphElement, GraphElement> lastAddedEquivalence;
    private final Map<GraphElement, GraphElement> mapL1toL2, mapL2toL1;
    private final HeapConfigurationContext context;

    protected JointMorphism(HeapConfigurationContext context, Collection<GraphElement> l1Remaining,
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
     * Initializes jointMorphism where all GraphElements are disjoint
     */
    protected JointMorphism(HeapConfigurationContext context, Collection<GraphElement> l1, Collection<GraphElement> l2) {
        this.context = context;
        l1Remaining = new TreeSet<>(l1);
        l2Remaining = new TreeSet<>(l2);
        lastAddedEquivalence = null;
        mapL1toL2 =  new HashMap<>();
        mapL2toL1 =  new HashMap<>();
    }


    protected JointMorphism(JointMorphism oldJointMorphism, Pair<GraphElement, GraphElement> newEquivalence) {
        context = oldJointMorphism.context;
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
     * Returns the joint morphisms that contains only a single equivalence more.
     * Furthermore the added equivalence has to come after the 'lastAddedEquivalence' from this object
     * according to the canonical ordering of node equivalences.
     * The isNextPairCompatible method is used to only include compatible GraphElement
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

    protected HeapConfigurationContext getContext() {
        return context;
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
     * Returns the JointMorphism if the newPair is added to the equivalences of this object.
     * @param newPair
     * @return
     */
    abstract JointMorphism getJointMorphism(Pair<GraphElement, GraphElement> newPair);

}

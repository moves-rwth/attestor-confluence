package de.rwth.i2.attestor.grammar.confluence.JointMorphism;

import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

/**
 * A joint-morphism is a morphism from two HCs hc1 and hc2 to an image hc H.
 * It can be directly classified by the nodes of hc1 and hc2 which map to the same node in H. These nodes can be
 * understood as node equivalences. Every joint-morphism therefore corresponds to a specific subset of the product of
 * the node sets of hc1 and hc2.
 */
public class JointMorphism {
    // TODO: This class can be further optimized by removing l1Remaining, which is not necessary
    private final TreeSet<Integer> l1Remaining, l2Remaining;
    private final Pair<Integer, Integer> lastAddedEquivalence;
    private final Map<Integer, Integer> mapL1toL2, mapL2toL1;

    /**
     * Initializes JointMorphism where all nodes are disjoint
     */
    public JointMorphism(TIntArrayList l1, TIntArrayList l2) {
        l1Remaining = new TreeSet<>();
        l1.forEach(i -> {
            l1Remaining.add(i);
            return true;
        });
        l2Remaining = new TreeSet<>();
        l2.forEach(i -> {
            l2Remaining.add(i);
            return true;
        });
        lastAddedEquivalence = null;
        mapL1toL2 =  new HashMap<>();
        mapL2toL1 =  new HashMap<>();
    }

    private JointMorphism(JointMorphism oldJointMorphism) {
        l1Remaining = new TreeSet<>(oldJointMorphism.l1Remaining);
        l2Remaining = new TreeSet<>(oldJointMorphism.l2Remaining);
        lastAddedEquivalence = oldJointMorphism.lastAddedEquivalence;
        mapL1toL2 =  new HashMap<>(oldJointMorphism.mapL1toL2);
        mapL2toL1 =  new HashMap<>(oldJointMorphism.mapL2toL1);
    }

    protected JointMorphism(JointMorphism oldJointMorphism, Pair<Integer, Integer> newEquivalence) {
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
    private Pair<Integer, Integer> getNextEquivalence(Pair<Integer, Integer> oldPair) {
        Integer l1New, l2New;
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
     */
    protected Collection<Pair<Integer, Integer>> getAllNextEquivalences() {
        Pair<Integer, Integer> nextNodeEquivalence;
        if (lastAddedEquivalence == null) {
            if (l1Remaining.isEmpty() || l2Remaining.isEmpty()) {
                throw new RuntimeException("First joint-morphism cannot be obtained.");
            }
            nextNodeEquivalence = new Pair<>(l1Remaining.first(), l2Remaining.first());
        } else {
            nextNodeEquivalence = getNextEquivalence(lastAddedEquivalence);
        }
        Collection<Pair<Integer, Integer>> result = new ArrayList<>();
        while (nextNodeEquivalence != null) {
            result.add(nextNodeEquivalence);
            nextNodeEquivalence = getNextEquivalence(nextNodeEquivalence);
        }
        return result;
    }

}

package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;
import org.jboss.util.Heap;
import org.omg.CORBA.INTERNAL;

import java.util.*;

public class JointMorphism {
    private final Set<Pair<Integer, Integer>> nodeEquivalence;
    private final TreeSet<Integer> hc1RemainingNodes, hc2RemainingNodes;
    private final Pair<Integer, Integer> lastAddedNodeEquivalence;
    private final Map<Integer, Integer> mapHc1toHc2Node, mapHc2toHc1Node;

    /**
     * Initializes JointMorphism where all nodes are disjoint
     */
    public JointMorphism(HeapConfiguration hc1, HeapConfiguration hc2) {
        nodeEquivalence = new HashSet<>();
        hc1RemainingNodes = new TreeSet<>();
        hc1.nodes().forEach(node -> {
            hc1RemainingNodes.add(node);
            return true;
        });
        hc2RemainingNodes = new TreeSet<>();
        hc2.nodes().forEach(node -> {
            hc2RemainingNodes.add(node);
            return true;
        });
        lastAddedNodeEquivalence = null;
        mapHc1toHc2Node =  new HashMap<>();
        mapHc2toHc1Node =  new HashMap<>();
    }

    private JointMorphism(JointMorphism oldJointMorphism) {
        nodeEquivalence = new HashSet<>(oldJointMorphism.nodeEquivalence);
        hc1RemainingNodes = new TreeSet<>(oldJointMorphism.hc1RemainingNodes);
        hc2RemainingNodes = new TreeSet<>(oldJointMorphism.hc2RemainingNodes);
        lastAddedNodeEquivalence = oldJointMorphism.lastAddedNodeEquivalence;
        mapHc1toHc2Node =  new HashMap<>(oldJointMorphism.mapHc1toHc2Node);
        mapHc2toHc1Node =  new HashMap<>(oldJointMorphism.mapHc2toHc1Node);
    }

    private JointMorphism(JointMorphism oldJointMorphism, Pair<Integer, Integer> newNodeEquivalence) {
        nodeEquivalence = new HashSet<>(oldJointMorphism.nodeEquivalence);
        hc1RemainingNodes = new TreeSet<>(oldJointMorphism.hc1RemainingNodes);
        hc1RemainingNodes.remove(newNodeEquivalence.first());
        hc2RemainingNodes = new TreeSet<>(oldJointMorphism.hc2RemainingNodes);
        hc2RemainingNodes.remove(newNodeEquivalence.second());
        lastAddedNodeEquivalence = newNodeEquivalence;
        nodeEquivalence.add(lastAddedNodeEquivalence);
        mapHc1toHc2Node =  new HashMap<>(oldJointMorphism.mapHc1toHc2Node);
        mapHc1toHc2Node.put(newNodeEquivalence.first(), newNodeEquivalence.second());
        mapHc2toHc1Node =  new HashMap<>(oldJointMorphism.mapHc2toHc1Node);
        mapHc2toHc1Node.put(newNodeEquivalence.second(), newNodeEquivalence.first());
    }

    /**
     * Returns the next possible successor for a node equivalence.
     * If there is no successor returns null
     */
    private Pair<Integer, Integer> getNextEquivalence(Pair<Integer, Integer> oldPair) {
        Integer firstNewNode, secondNewNode;
        secondNewNode = hc2RemainingNodes.higher(oldPair.second());
        if (secondNewNode == null) {
            // If there is no higher node in hc2 the next equivalence we look for a higher node in hc1
            firstNewNode = hc1RemainingNodes.higher(oldPair.first());
            if (firstNewNode == null) {
                // There are no more valid nodes in hc1
                return null;
            }
            // Start again by the lowest available node in hc2
            secondNewNode = hc2RemainingNodes.first();
            if (secondNewNode == null) {
                // There are no more valid nodes in hc2
                return null;
            }
        } else {
            firstNewNode = oldPair.first();
        }
        return new Pair<>(firstNewNode, secondNewNode);
    }

    /**
     * Returns the joint morphisms that contains only a single node equivalence more and who are
     */
    public Collection<JointMorphism> getFollowingJointMorphisms() {
        Pair<Integer, Integer> nextNodeEquivalence;
        if (lastAddedNodeEquivalence == null) {
            if (hc1RemainingNodes.isEmpty() || hc2RemainingNodes.isEmpty()) {
                throw new RuntimeException("First joint-morphism cannot be obtained.");
            }
            nextNodeEquivalence = new Pair<>(hc1RemainingNodes.first(), hc2RemainingNodes.first());
        } else {
            nextNodeEquivalence = getNextEquivalence(lastAddedNodeEquivalence);
        }
        Collection<JointMorphism> result = new ArrayList<>();
        while (nextNodeEquivalence != null) {
            result.add(new JointMorphism(this, nextNodeEquivalence));
            nextNodeEquivalence = getNextEquivalence(nextNodeEquivalence);
        }
        return result;
    }
}

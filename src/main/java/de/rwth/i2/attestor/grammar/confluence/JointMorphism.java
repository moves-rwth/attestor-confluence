package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.types.Types;
import de.rwth.i2.attestor.util.Pair;
import org.jboss.util.Heap;
import org.omg.CORBA.INTERNAL;

import java.util.*;

/**
 * A joint-morphism is a morphism from two HCs hc1 and hc2 to an image hc H.
 * It can be directly classified by the nodes of hc1 and hc2 which map to the same node in H. These nodes can be
 * understood as node equivalences. Every joint-morphism therefore corresponds to a specific subset of the product of
 * the node sets of hc1 and hc2.
 */
public class JointMorphism {
    // TODO: This class can be further optimized by removing hc1RemainingNodes, which is not necessary
    private final TreeSet<Integer> hc1RemainingNodes, hc2RemainingNodes;
    private final Pair<Integer, Integer> lastAddedNodeEquivalence;
    private final Map<Integer, Integer> mapHc1toHc2Node, mapHc2toHc1Node;
    // The following variables might be needed in order to check if
    private final Set<Integer> criticalNonTerminalEdgesHc1, criticalNonTerminalEdgesHc2;

    /**
     * Initializes JointMorphism where all nodes are disjoint
     */
    public JointMorphism(HeapConfiguration hc1, HeapConfiguration hc2) {
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
        hc1RemainingNodes = new TreeSet<>(oldJointMorphism.hc1RemainingNodes);
        hc2RemainingNodes = new TreeSet<>(oldJointMorphism.hc2RemainingNodes);
        lastAddedNodeEquivalence = oldJointMorphism.lastAddedNodeEquivalence;
        mapHc1toHc2Node =  new HashMap<>(oldJointMorphism.mapHc1toHc2Node);
        mapHc2toHc1Node =  new HashMap<>(oldJointMorphism.mapHc2toHc1Node);
    }

    private JointMorphism(JointMorphism oldJointMorphism, Pair<Integer, Integer> newNodeEquivalence) {
        hc1RemainingNodes = new TreeSet<>(oldJointMorphism.hc1RemainingNodes);
        hc1RemainingNodes.remove(newNodeEquivalence.first());
        hc2RemainingNodes = new TreeSet<>(oldJointMorphism.hc2RemainingNodes);
        hc2RemainingNodes.remove(newNodeEquivalence.second());
        lastAddedNodeEquivalence = newNodeEquivalence;
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
     * Returns the joint morphisms that contains only a single node equivalence more.
     * Furthermore the added node equivalence has to come after the 'lastAddedNodeEquivalence' from this object
     * according to the canonical ordering of node equivalences.
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

    public JointMorphismCompatibility isJointMorphismCompatibile(HeapConfiguration hc1, HeapConfiguration hc2) {
        final int lastAddedNode1  = lastAddedNodeEquivalence.first();
        final int lastAddedNode2  = lastAddedNodeEquivalence.second();

        // 1. Check that node types are compatible
        Type typeNode1 = hc1.nodeTypeOf(lastAddedNode1);
        Type typeNode2 = hc2.nodeTypeOf(lastAddedNode2);
        if (!typeNode1.matches(typeNode2)) {
            // TODO: Check if the usage of "matches" is correct here or if we should use "equals"
            // The nodes types do not match --> Incompatible morphism
            return JointMorphismCompatibility.INCOMPATIBLE;
        }

        // TODO 2. Check that attached non terminal edges are compatible
        hc1.attachedNonterminalEdgesOf(lastAddedNode1);
        hc2.attachedNonterminalEdgesOf(lastAddedNode2);

        // TODO 3. Check that variables edges are compatible
        hc1.attachedVariablesOf(lastAddedNode1);
        hc2.attachedVariablesOf(lastAddedNode2);

        // TODO 4. Check that selector edges are compatible
        hc1.selectorLabelsOf(lastAddedNode1).forEach(sel1 -> {

            return true;
        });

        hc2.selectorLabelsOf(lastAddedNode2);

        return JointMorphismCompatibility.INCOMPATIBLE;
    }

    enum JointMorphismCompatibility {
        COMPATIBLE, INCOMPATIBLE, NOT_COMPATIBLE_YET;

    }
}

package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * A class that represents possible overlappings of hyperedges between two graphs in a graph.
 * It also keeps track of the necessary node overlappings.
 */
public class EdgeJointMorphism extends JointMorphism {
    final Map<Integer, Integer> mapNodeHc1ToHc2, mapNodeHc2ToHc1;

    public EdgeJointMorphism(Collection<GraphElement> l1, Collection<GraphElement> l2) {
        super(l1, l2);
        this.mapNodeHc1ToHc2 = new HashMap<>();
        this.mapNodeHc2ToHc1 = new HashMap<>();
    }

    protected EdgeJointMorphism(JointMorphism oldJointMorphism, Pair<GraphElement, GraphElement> newEquivalence) {
        super(oldJointMorphism, newEquivalence);
        if (oldJointMorphism instanceof EdgeJointMorphism) {
            this.mapNodeHc1ToHc2 = new HashMap<>(((EdgeJointMorphism) oldJointMorphism).mapNodeHc1ToHc2);
            this.mapNodeHc2ToHc1 = new HashMap<>(((EdgeJointMorphism) oldJointMorphism).mapNodeHc2ToHc1);
        } else {
            this.mapNodeHc1ToHc2 = new HashMap<>();
            this.mapNodeHc2ToHc1 = new HashMap<>();
        }
        // Add node equivalences induced by the added edge
        // TODO

    }

}

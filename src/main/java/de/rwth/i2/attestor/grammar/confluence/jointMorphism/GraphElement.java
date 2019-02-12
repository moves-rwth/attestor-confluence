package de.rwth.i2.attestor.grammar.confluence.jointMorphism;


import de.rwth.i2.attestor.graph.morphism.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents nodes, nonterminal edges, variables and selectors from used in {@link de.rwth.i2.attestor.graph.morphism.Graph}.
 *
 * The elements are ordered according to the following rules (first element is privateId second element is selectorLabel):
 * (i, _) < (j, _)             if i < j
 * (i, null) < (i, label)      if label != null
 * (i, label1) < (i, label2)   if label1 != null && label2 != null && label1 < label2 (using regular string comparison)
 *
 * @author Johannes Schulte
 */
public class GraphElement implements Comparable<GraphElement> {
    /**
     * The privateId of either the edge itself (in case of nonterminal or variable edge) or of the source node of a selector.
     */
    private final int privateId;

    /**
     * In case of a selector edge this contains the label of the selector. Otherwise this is null.
     */
    private final String selectorLabel;

    public GraphElement(int privateId, String selectorLabel) {
        this.privateId = privateId;
        this.selectorLabel = selectorLabel;
    }

    public int getPrivateId() {
        return privateId;
    }

    public String getSelectorLabel() {
        return selectorLabel;
    }

    @Override
    public int compareTo(GraphElement graphElement) {
        if (privateId != graphElement.privateId) {
            // If the private id is different -> order is based on it
            return privateId - graphElement.privateId;
        } else if (selectorLabel == null) {
            if (graphElement.selectorLabel == null) {
                // PrivateId and selectorLabel match   this=(i, null) == other=(i, null)
                return 0;
            } else {
                // this=(i, null) < other=(i, label)
                return -1;
            }
        } else {
            // The privateId is the same
            if (graphElement.selectorLabel == null) {
                // this=(i, label) > other=(i, null)
                return 1;
            } else {
                // Order is based on the comparison of the label strings
                return selectorLabel.compareTo(graphElement.selectorLabel);
            }

        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateId, selectorLabel);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GraphElement) {
            GraphElement otherGraphElement = (GraphElement) o;
            return this.compareTo(otherGraphElement) == 0;
        } else {
            return false;
        }
    }

    /**
     * Returns a list of GraphElements that represent the nodes connected to this edge. This object must be an edge in
     * the given graph.
     *
     * @param graph  A graph in which this object must be an edge.
     * @return
     */
    public List<GraphElement> getConnectedNodes(Graph graph) {
        List<GraphElement> result = new ArrayList<>();
        graph.getSuccessorsOf(privateId).forEach(nodePid -> {
            result.add(new GraphElement(nodePid, null));
            return true;
        });
        return result;
    }

}

package de.rwth.i2.attestor.grammar.confluence.jointMorphism;


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
}

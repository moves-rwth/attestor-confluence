package de.rwth.i2.attestor.grammar.confluence.jointMorphism;


/**
 * Represents nodes, nonterminal edges, variables and selectors from used in {@link de.rwth.i2.attestor.graph.morphism.Graph}.
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
            return privateId - graphElement.privateId;
        } else {
            return selectorLabel.compareTo(graphElement.selectorLabel);
        }
    }
}

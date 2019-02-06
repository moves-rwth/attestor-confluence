package de.rwth.i2.attestor.graph.heap.bipartite;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.internal.InternalHeapConfiguration;
import de.rwth.i2.attestor.graph.heap.matching.AbstractMatchingChecker;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.graph.morphism.MorphismOptions;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;

import java.util.List;


/**
 * This class represents a HeapConfiguration similar to {@link HeapConfiguration} but ensures that the underlying
 * Graph is bipartite. This is done by adding selectors in the same way as nonterminal edges.
 */
public class BipartiteHeapConfiguration implements HeapConfiguration, Graph {
    private final HeapConfiguration wrappedHeapConfiguration;

    /**
     * Creates a Heapconfiguration
     * @param hc
     */
    private BipartiteHeapConfiguration(HeapConfiguration hc) {
        this.wrappedHeapConfiguration = hc;
    }

    /**
     * Converts the HeapConfiguration to a BipartiteHeapConfiguration.
     *
     * @param hc
     */
    public static BipartiteHeapConfiguration convertToBipartiteHeapConfiguration(HeapConfiguration hc) {
        // TODO
    }

    //TODO: Remove this comment  ##### HeapConfiguration interface methods following ######
    @Override
    public HeapConfiguration clone() {
        return wrappedHeapConfiguration
    }

    @Override
    public HeapConfiguration getEmpty() {
        return new BipartiteHeapConfiguration(wrappedHeapConfiguration.getEmpty());
    }

    @Override
    public HeapConfigurationBuilder builder() {
        return null;
    }

    @Override
    public int countNodes() {
        return wrappedHeapConfiguration.countNodes();
    }

    @Override
    public TIntArrayList nodes() {
        return wrappedHeapConfiguration.nodes();
    }

    @Override
    public Type nodeTypeOf(int node) {
        return wrappedHeapConfiguration.nodeTypeOf(node);
    }

    @Override
    public TIntArrayList attachedVariablesOf(int node) {
        return wrappedHeapConfiguration.attachedVariablesOf(node);
    }

    @Override
    public TIntArrayList attachedNonterminalEdgesOf(int node) {
        // TODO: Implement additional filter for nonterminal edges
        return null;
    }

    @Override
    public TIntArrayList successorNodesOf(int node) {
        // TODO
        return null;
    }

    @Override
    public TIntArrayList predecessorNodesOf(int node) {
        // TODO
        return null;
    }

    @Override
    public List<SelectorLabel> selectorLabelsOf(int node) {
        // TODO
        return null;
    }

    @Override
    public int selectorTargetOf(int node, SelectorLabel sel) {
        // TODO
        return 0;
    }

    @Override
    public int countExternalNodes() {
        return 0;
    }

    @Override
    public TIntArrayList externalNodes() {
        return null;
    }

    @Override
    public int externalNodeAt(int pos) {
        return 0;
    }

    @Override
    public boolean isExternalNode(int node) {
        return false;
    }

    @Override
    public int externalIndexOf(int node) {
        return 0;
    }

    @Override
    public int countNonterminalEdges() {
        return 0;
    }

    @Override
    public TIntArrayList nonterminalEdges() {
        return null;
    }

    @Override
    public int rankOf(int ntEdge) {
        return 0;
    }

    @Override
    public Nonterminal labelOf(int ntEdge) {
        return null;
    }

    @Override
    public TIntArrayList attachedNodesOf(int ntEdge) {
        return null;
    }

    @Override
    public int countVariableEdges() {
        return 0;
    }

    @Override
    public TIntArrayList variableEdges() {
        return null;
    }

    @Override
    public int variableWith(String name) {
        return 0;
    }

    @Override
    public String nameOf(int varEdge) {
        return null;
    }

    @Override
    public int targetOf(int varEdge) {
        return 0;
    }

    @Override
    public AbstractMatchingChecker getEmbeddingsOf(HeapConfiguration pattern, MorphismOptions options) {
        return null;
    }

    @Override
    public int variableTargetOf(String variableName) {
        return 0;
    }

    @Override
    public TIntIntMap attachedNonterminalEdgesWithNonReductionTentacle(int node) {
        return null;
    }

    //TODO: Remove this comment  ##### Graph interface methods following ######

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean hasEdge(int from, int to) {
        return false;
    }

    @Override
    public TIntArrayList getSuccessorsOf(int node) {
        return null;
    }

    @Override
    public TIntArrayList getPredecessorsOf(int node) {
        return null;
    }

    @Override
    public NodeLabel getNodeLabel(int node) {
        return null;
    }

    @Override
    public List<Object> getEdgeLabel(int from, int to) {
        return null;
    }

    @Override
    public boolean isExternal(int node) {
        return false;
    }

    @Override
    public int getExternalIndex(int node) {
        return 0;
    }

    @Override
    public boolean isEdgeBetweenMarkedNodes(int from, int to) {
        return false;
    }
}

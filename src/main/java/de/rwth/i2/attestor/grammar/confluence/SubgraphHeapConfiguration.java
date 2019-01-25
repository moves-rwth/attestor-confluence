package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.matching.AbstractMatchingChecker;
import de.rwth.i2.attestor.graph.morphism.MorphismOptions;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Set;

public class SubgraphHeapConfiguration implements HeapConfiguration {
    HeapConfiguration superGraph;
    Set<Integer> includedNodes;
    TIntArrayList externalNodes;
    Set<Integer> edgeSet;
    Set<Integer> variableSet;

    /**
     * Create a subgraph by specifying a set of nodes
     * For performance reasons it is not verified if these nodes are valid public ids of nodes in the supergraph.
     * If there are invalid nodes this class might behave unexpected.
     * The public id of all elements of the supergraph stays the same
     *
     * @param superGraph The supergraph
     * @param nodes The set of nodes from the supergraph that should be included in the subgraph
     */
    public SubgraphHeapConfiguration(HeapConfiguration superGraph, Set<Integer> nodes) {
        this.superGraph = superGraph;
        this.includedNodes = nodes;
        this.externalNodes = new TIntArrayList();
        superGraph.externalNodes().forEach(node -> {
            if (this.isExternalNode(node)) {
                this.externalNodes.add(node);
            }
            return true;
        });

    }

    @Override
    public HeapConfiguration clone() {
        throw new NotImplementedException();
    }

    @Override
    public HeapConfiguration getEmpty() {
        throw new NotImplementedException();
    }

    @Override
    public HeapConfigurationBuilder builder() {
        throw new NotImplementedException();
    }

    @Override
    public int countNodes() {
        return includedNodes.size();
    }

    @Override
    public TIntArrayList nodes() {
        TIntArrayList result = new TIntArrayList();
        for (int node : includedNodes)
            result.add(node);
        return result;
    }

    @Override
    public Type nodeTypeOf(int node) {
        if (!includedNodes.contains(node))
            throw new IllegalArgumentException("Provided ID does not correspond to a node");
        return superGraph.nodeTypeOf(node);
    }

    @Override
    public TIntArrayList attachedVariablesOf(int node) {
        if (!includedNodes.contains(node))
            throw new IllegalArgumentException("Provided ID does not correspond to a node");
        return superGraph.attachedVariablesOf(node);
    }

    @Override
    public TIntArrayList attachedNonterminalEdgesOf(int node) {
        if (!includedNodes.contains(node))
            throw new IllegalArgumentException("Provided ID does not correspond to a node");
        TIntArrayList result = new TIntArrayList();
        superGraph.attachedNonterminalEdgesOf(node).forEach(edge -> {
            if (edgeSet.contains(edge)) {
                result.add(edge);
            }
        })
        return result;
    }

    @Override
    public TIntArrayList successorNodesOf(int node) {
        if (!includedNodes.contains(node))
            throw new IllegalArgumentException("Provided ID does not correspond to a node");
        return superGraph.successorNodesOf(node);
    }

    @Override
    public TIntArrayList predecessorNodesOf(int node) {
        if (!includedNodes.contains(node))
            throw new IllegalArgumentException("Provided ID does not correspond to a node");
        return superGraph.predecessorNodesOf(node);
    }

    @Override
    public List<SelectorLabel> selectorLabelsOf(int node) {
        if (!includedNodes.contains(node))
            throw new IllegalArgumentException("Provided ID does not correspond to a node");
        return superGraph.selectorLabelsOf(node);
    }

    @Override
    public int selectorTargetOf(int node, SelectorLabel sel) {
        if (!includedNodes.contains(node))
            throw new IllegalArgumentException("Provided ID does not correspond to a node");
        return superGraph.selectorTargetOf(node, sel);
    }

    @Override
    public int countExternalNodes() {
        return this.externalNodes.size();
    }

    @Override
    public TIntArrayList externalNodes() {
        return externalNodes;
    }

    @Override
    public int externalNodeAt(int pos) {
        return externalNodes.get(pos);
    }

    @Override
    public boolean isExternalNode(int node) {
        if (!includedNodes.contains(node))
            throw new IllegalArgumentException("Provided ID does not correspond to a node");
        return superGraph.isExternalNode(node);
    }

    @Override
    public int externalIndexOf(int node) {
        return externalNodes.indexOf(node);
    }

    @Override
    public int countNonterminalEdges() {
        return nonterminalEdges().size();
    }

    @Override
    public TIntArrayList nonterminalEdges() {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public int rankOf(int ntEdge) {
        return superGraph.rankOf(ntEdge);
    }

    @Override
    public Nonterminal labelOf(int ntEdge) {
        return superGraph.labelOf(ntEdge);
    }

    @Override
    public TIntArrayList attachedNodesOf(int ntEdge) {
        return superGraph.attachedNodesOf(ntEdge);
    }

    @Override
    public int countVariableEdges() {
        return this.variableEdges().size();
    }

    @Override
    public TIntArrayList variableEdges() {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public int variableWith(String name) {
        return superGraph.variableWith(name);
    }

    @Override
    public String nameOf(int varEdge) {
        return superGraph.nameOf(varEdge);
    }

    @Override
    public int targetOf(int varEdge) {
        return superGraph.targetOf(varEdge);
    }

    @Override
    public AbstractMatchingChecker getEmbeddingsOf(HeapConfiguration pattern, MorphismOptions options) {
        throw new NotImplementedException();
    }

    @Override
    public int variableTargetOf(String variableName) {
        return superGraph.variableTargetOf(variableName);
    }

    @Override
    public TIntIntMap attachedNonterminalEdgesWithNonReductionTentacle(int node) {
        //TODO
        throw new NotImplementedException();
    }
}

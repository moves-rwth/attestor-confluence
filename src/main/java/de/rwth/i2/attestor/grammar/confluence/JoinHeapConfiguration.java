package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.matching.AbstractMatchingChecker;
import de.rwth.i2.attestor.graph.morphism.MorphismOptions;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;

import java.util.List;

public class JoinHeapConfiguration implements HeapConfiguration {
    HeapConfiguration hc1, hc2;


    /**
     * Joins two Heap configurations while keeping some nodes the same.
     *
     * @param hc1
     * @param hc2
     * @param matchingNodes
     */

    public JoinHeapConfiguration(HeapConfiguration hc1, HeapConfiguration hc2, List<Pair<Integer, Integer>> matchingNodes) {
        this.hc1 = hc1;
        this.hc2 = hc2;
    }

    @Override
    public HeapConfiguration clone() {
        return null;
    }

    @Override
    public HeapConfiguration getEmpty() {
        return null;
    }

    @Override
    public HeapConfigurationBuilder builder() {
        return null;
    }

    @Override
    public int countNodes() {
        return 0;
    }

    @Override
    public TIntArrayList nodes() {
        return null;
    }

    @Override
    public Type nodeTypeOf(int node) {
        return null;
    }

    @Override
    public TIntArrayList attachedVariablesOf(int node) {
        return null;
    }

    @Override
    public TIntArrayList attachedNonterminalEdgesOf(int node) {
        return null;
    }

    @Override
    public TIntArrayList successorNodesOf(int node) {
        return null;
    }

    @Override
    public TIntArrayList predecessorNodesOf(int node) {
        return null;
    }

    @Override
    public List<SelectorLabel> selectorLabelsOf(int node) {
        return null;
    }

    @Override
    public int selectorTargetOf(int node, SelectorLabel sel) {
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
}

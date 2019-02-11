package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.*;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;

import java.util.*;


/**
 *
 * Computes the critical pairs of a grammar on construction.
 * Provides methods to access information about the critical pairs.
 *
 * The implemented algorithm to find the critical pairs is based on the work in: "Efficient Detection of Conflicts in
 * Graph-based Model Transformation" by Leen Lambers, Hartmut Ehrig & Fernando Orejas
 * TODO: How to correctly cite in javadoc
 *
 * @author Johannes Schulte
 */
public class CriticalPairFinder {

    final Grammar underlyingGrammar;
    final Set<CriticalPair> criticalPairs;

    public CriticalPairFinder(Grammar grammar) {
        this.underlyingGrammar = grammar;
        this.criticalPairs = new HashSet<>();
        computeAllCriticalPairs();
    }

    private void computeAllCriticalPairs() {
        Set<CriticalPair> criticalPairs = new HashSet<>();
        // Add critical pairs for all combinations of rules

        // 1. Create a list with all *individual* grammar rules
        List<Pair<Nonterminal, CollapsedHeapConfiguration>> individualGrammarRules = new ArrayList<>();
        for (Nonterminal nonterminal : this.underlyingGrammar.getAllLeftHandSides()) {
            for (CollapsedHeapConfiguration heapConfiguration : this.underlyingGrammar.getCollapsedRightHandSidesFor(nonterminal)) {
                individualGrammarRules.add(new Pair<>(nonterminal, heapConfiguration));
            }
        }

        // 2. Iterate over all pairs of individual grammar rules and add the critical pairs for each pair
        for (int i = 0; i < individualGrammarRules.size(); i++) {
            for (int j = i; j < individualGrammarRules.size(); j++) {
                Pair<Nonterminal, CollapsedHeapConfiguration> r1 = individualGrammarRules.get(i);
                Pair<Nonterminal, CollapsedHeapConfiguration> r2 = individualGrammarRules.get(j);
                addCriticalPairsForCollapsedRule(r1, r2);
            }
        }
    }



    /**
     * This method computes all possible jointly surjective morphisms g1, g2 such that (g1: l1 -> s, g2: l2 -> s)
     * for the two right hand sides (l1, l2) of the rules r1, r2.
     * For each of these morphisms we check if it induces a critical pair.
     *
     * @param r1 The first rule
     * @param r2 The second rule
     */
    private void addCriticalPairsForCollapsedRule(Pair<Nonterminal, CollapsedHeapConfiguration> r1,
                                                  Pair<Nonterminal, CollapsedHeapConfiguration> r2) {
        HeapConfiguration hc1 = r1.second().getCollapsed();
        HeapConfiguration hc2 = r2.second().getCollapsed();
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);


        for (JointMorphism edgeMorphism : new EdgeJointMorphism(context)) {
            for (JointMorphism nodeMorphism : new NodeJointMorphism(context, (EdgeJointMorphism) edgeMorphism)) {
                // Found a compatible joint morphism
                // 1. Compute the joint graph
                // TODO

                // 2. Compute fully abstracted heap configuration (apply r1 first)
                // TODO

                // 3. Compute fully abstracted heap configuration (apply r2 first)
                // TODO

                // 4. Check if both fully abstracted heap configurations are isomorphic
                // TODO
            }
        }
    }


    /**
     * Converts the nodes of a {@link HeapConfiguration} into a collection of {@link GraphElement}.
     *
     * @param hc The input heap configuration (must be of type {@link Graph})
     * @return A collection containing all nodes of hc
     */
    private Collection<GraphElement> getNodes(HeapConfiguration hc) {
        if (!(hc instanceof Graph)) {
            throw new IllegalArgumentException("HeapConfiguration not of type Graph");
        }
        Collection<GraphElement> result = new ArrayList<>();
        Graph graph = (Graph) hc;
        for (int privateId = 0; privateId < graph.size(); privateId++) {
            NodeLabel label = graph.getNodeLabel(privateId);
            if (label instanceof Type) {
                // The current privateId corresponds to a node in hc
                result.add(new GraphElement(privateId, null));
            }
        }
        return result;
    }

    /**
     * Converts the nonterminal edges and selectors of a {@link HeapConfiguration} into a collection of
     * {@link GraphElement}.
     *
     * @param hc The input heap configuration (must be of type {@link Graph})
     * @return A collection containing all nonterminal edges and selectors of hc
     */
    private Collection<GraphElement> getEdges(HeapConfiguration hc) {
        if (!(hc instanceof Graph)) {
            throw new IllegalArgumentException("HeapConfiguration not of type Graph");
        }
        Collection<GraphElement> result = new ArrayList<>();
        Graph graph = (Graph) hc;
        for (int privateId = 0; privateId < graph.size(); privateId++) {
            NodeLabel label = graph.getNodeLabel(privateId);
            if (label instanceof Nonterminal) {
                // The current privateId corresponds to a nonterminal edge
                result.add(new GraphElement(privateId, null));
            } else if (label instanceof Type) {
                // The current privateId is a node. Check if there are any outgoing selectors
                final int finalPrivateId = privateId; // variable must be final to be used in lambda expression later
                graph.getSuccessorsOf(privateId).forEach(successor -> {
                   for (Object edgeLabel : graph.getEdgeLabel(finalPrivateId, successor)) {
                       if (edgeLabel instanceof SelectorLabel) {
                           // There is a selector from privateId to successor
                           String selectorLabel = ((SelectorLabel) edgeLabel).getLabel();
                           result.add(new GraphElement(finalPrivateId, selectorLabel));
                       }
                   }
                   return true;
                });
            }
        }
        return result;
    }



}

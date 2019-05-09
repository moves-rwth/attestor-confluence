package de.rwth.i2.attestor.grammar.confluence.completion.validity;

import com.google.common.collect.ImmutableMap;
import de.rwth.i2.attestor.grammar.GrammarRuleOriginal;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.typedness.GrammarTypedness;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import gnu.trove.list.array.TIntArrayList;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Ensures that the modified grammar is still a data structure grammar
 * (i.e. it does not allow multiple outgoing selectors of the same type)
 *
 * Check for all RHS that the sets of direct outgoing selectors and the set of recursive reachable outgoing selectors
 * for each attached tentacle does not overlap
 */
public class CheckDataStructureGrammar implements GrammarValidity {
    @Override
    public boolean isValid(CompletionState newCompletionState) {
        GrammarTypedness types = newCompletionState.getTypes();
        for (GrammarRuleOriginal rule : newCompletionState.getGrammar().getOriginalGrammarRules()) {
            HeapConfiguration rhs = rule.getHeapConfiguration();
            TIntArrayList nodes = rhs.nodes();
            for (int nodeIdx = 0; nodeIdx < nodes.size(); nodeIdx++) {
                int node = nodes.get(nodeIdx);
                Set<SelectorLabel> outgoingSelectors = new HashSet<>(rhs.selectorLabelsOf(node));
                // Check if all recursive creatable selectors are disjoint
                TIntArrayList nonterminals = rhs.attachedNonterminalEdgesOf(node);
                for (int ntIdx = 0; ntIdx < nonterminals.size(); ntIdx++) {
                    int ntEdge = nonterminals.get(ntIdx);
                    Nonterminal nonterminal = rhs.labelOf(ntEdge);
                    TIntArrayList attachedNodes = rhs.attachedNodesOf(ntEdge);
                    for (int tentacle = 0; tentacle < attachedNodes.size(); tentacle++) {
                        int attachedNodeId = attachedNodes.get(tentacle);
                        if (node == attachedNodeId) {
                            // Found a tentacle of the node
                            Set<SelectorLabel> recursiveOutgoingSelectors = types.getTentacleType(nonterminal, tentacle).getAllTypes();
                            // Check that the selectors are not already contained in the outgoingSelectors set
                            if (!Collections.disjoint(outgoingSelectors, recursiveOutgoingSelectors)) {
                                System.err.println("No a data structure grammar");
                                return false;
                            }
                            // Add the selectors to the set
                            outgoingSelectors.addAll(recursiveOutgoingSelectors);
                        }
                    }
                }


            }
        }
        return true;
    }

    @Override
    public JSONObject getDescription() {
        return new JSONObject(ImmutableMap.of(
                "name", "checkDatastructureGrammar"
        ));
    }

}

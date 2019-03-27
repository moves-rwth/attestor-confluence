package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;

/**
 * For a critical pair this method adds two new rules that introduce a new nonterminal
 */
public class AddNewNonterminalRuleHeuristic implements CompletionRuleAddingHeuristic {
    @Override
    public Collection<Pair<Nonterminal, CollapsedHeapConfiguration>> addNewRules(CriticalPair criticalPair) {
        // TODO: Implement
        return null;
    }
}

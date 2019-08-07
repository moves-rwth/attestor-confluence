package de.rwth.i2.attestor.grammar.confluence.completion.validity;

import com.google.common.collect.ImmutableMap;
import de.rwth.i2.attestor.grammar.*;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.typedness.GrammarTypedness;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import org.json.JSONObject;

import java.util.*;

/**
 * Ensures that the modified grammar is still local concretisable, given that the input grammar is local concretisable.
 *
 * The check might produce false negatives (returning not local concretisable, even if the grammar is local concretisable)
 *
 * It is recommended to use this class only in conjunction with the "AddRulesNewNonterminalHeuristic",
 * "JoinGeneratedNonterminalsHeuristic" and "SingleNonterminalRuleAddingHeuristic".
 *
 */
public class LocalConcretizability implements GrammarValidity {
    int numSuccess = 0;
    int numFailure = 0;

    /**
     * Assuming the old grammar was locally concretizable is the new grammar locally concretizable?
     * Note: Might return false negatives (returns false even though locally concretizable)
     *       If a rule cannot create an outgoing selector immediately, but a recursive and all HCs with that selector
     *       are already created by different rules that can create the selector immediately
     *
     * For all tentacles of all LHS of newRules:
     * 1. Calculate the set of outgoing selectors for each RHS at the current tentacle
     * 2. If there is a selector edge that is not in one RHS, check if the RHS can create the selector edge using further rule applications -> violation
     *
     * Work in Progress:
     * - Always try to calculate local concretizability based on original grammar first
     *   - If all LHS of the new rules are not contained in the old rules -> Only look at new rules
     *   - Otherwise: Need to check all rules
     * - For each rule that needs to be checked:
     *    - For every tentacle: 1. Obtain the set I (immediate outgoing selectors)
     *                          2. Obtain the set R (recursive outgoing selectors at the tentacle after applying given rule)
     *                          3. If (R \ I) is not empty => Violation
     *
     *                          Alternative:
     *                          - Obtain the set T (recursive outgoing selectors at the tentacle after applying ANY rule)
     *                          - (T \ I) cap R  is not empty => violation
     *
     *
     * TODO: What about the deactivation of collapsed rules? => Is the complete recalculation required?
     * TODO: Are there any cases regarding collapsed rules I have not thought about?
     */
    @Override
    public boolean isValid(CompletionState newCompletionState) {
        if (checkLocalConcretizability(newCompletionState.getGrammar(), newCompletionState.getTypes(), false)) {
            numSuccess++;
            return true;
        } else {
            numFailure++;
            return false;
        }
    }

    public static boolean checkLocalConcretizability(NamedGrammar grammar, GrammarTypedness types, boolean forceCompleteCheck) {
        Collection<GrammarRuleOriginal> newGrammarRules;
        if (forceCompleteCheck) {
            newGrammarRules = grammar.getOriginalGrammarRules();
        } else {
            newGrammarRules = getRulesToCheck(grammar);
        }

        if (newGrammarRules.size() == 0) {
            // No rules were added -> Local concretizability remains intact
            return true;
        } else {
            // Some rules were added -> Check if they don't violate local concretizability
            Grammar newRules = getGrammarFromOriginalRules(newGrammarRules);

            for (Nonterminal nt : newRules.getAllLeftHandSides()) {
                for (int tentacle=0; tentacle < nt.getRank(); tentacle++) {
                    for (HeapConfiguration newRhs : newRules.getRightHandSidesFor(nt)) {
                        int node = newRhs.externalNodeAt(tentacle);
                        Set<SelectorLabel> directNewSelectors = new HashSet<>(newRhs.selectorLabelsOf(node));
                        Set<SelectorLabel> recursiveNewSelectors = types.getTypesAtNode(newRhs, node);
                        if (!directNewSelectors.containsAll(recursiveNewSelectors)) {
                            // The rule can create an outgoing selector recursively, but not immediately (connected to non reduction tentacle)
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    private static Grammar getGrammarFromOriginalRules(Iterable<GrammarRuleOriginal> rules) {
        GrammarBuilder builder = new GrammarBuilder();
        for (GrammarRuleOriginal rule : rules) {
            builder.addRule(rule.getNonterminal(), rule.getHeapConfiguration());
            for (GrammarRuleCollapsed collapsed : rule.getCollapsedRules()) {
                builder.addCollapsedRule(collapsed.getNonterminal(), collapsed.getCollapsedHeapConfiguration());
            }
        }
        return builder.build();
    }


    private static Collection<GrammarRuleOriginal> getRulesToCheck(NamedGrammar grammar) {
        // Compute the set of old nonterminals (don't need to check the RHS, because if a nonterminal is in the RHS it should also be a LHS)
        Set<Nonterminal> oldNonterminals = new HashSet<>();
        Collection<GrammarRuleOriginal> generatedRules = new ArrayList<>();

        for (GrammarRuleOriginal original : grammar.getOriginalGrammarRules()) {
            if (original.getRuleStatus() == GrammarRule.RuleStatus.CONFLUENCE_GENERATED) {
                generatedRules.add(original);
            } else if (original.getRuleStatus() == GrammarRule.RuleStatus.INACTIVE) {
                // Complete recalculation required
                return grammar.getOriginalGrammarRules();
            } else { // Is handwritten original rule
                for (GrammarRuleCollapsed grammarRuleCollapsed : original.getCollapsedRules()) {
                    if (grammarRuleCollapsed.getRuleStatus() == GrammarRule.RuleStatus.INACTIVE) {
                        return grammar.getOriginalGrammarRules();
                    }
                }
            }
        }

        return generatedRules;
    }

    @Override
    public JSONObject getDescription() {
        return new JSONObject(ImmutableMap.of(
                "name", "localConcretizability",
                "numSuccess", numSuccess,
                "numFailure", numFailure
        ));
    }

}

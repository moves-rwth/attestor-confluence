package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.grammar.util.ExternalNodesPartitioner;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

public class NamedGrammar extends Grammar {
    final String grammarName;
    final List<Pair<Nonterminal, HeapConfiguration>> originalRules;
    final List<List<CollapsedHeapConfiguration>> collapsedRules;

    public NamedGrammar(Grammar grammar, String name) {
        super(grammar.rules, grammar.collapsedRules);
        originalRules = new ArrayList<>();
        collapsedRules = new ArrayList<>();
        grammarName = name;

        for(Map.Entry<Nonterminal, Set<HeapConfiguration>> entry : rules.entrySet()) {

            Nonterminal nonterminal = entry.getKey();
            boolean[] reductionTentacles = new boolean[nonterminal.getRank()];
            for(int i=0; i < nonterminal.getRank(); i++) {
                reductionTentacles[i] = nonterminal.isReductionTentacle(i);
            }

            for(HeapConfiguration hc : entry.getValue()) {
                // Add original rule
                originalRules.add(new Pair<>(nonterminal, hc));
                // Add collapsed rules
                List<CollapsedHeapConfiguration> collapsedRulesOfCurrentRule = new ArrayList<>();
                collapsedRules.add(collapsedRulesOfCurrentRule);
                ExternalNodesPartitioner partitioner = new ExternalNodesPartitioner(hc, reductionTentacles);
                for(TIntArrayList extIndexPartition : partitioner.getPartitions()) {
                    HeapConfiguration collapsedHc = hc.clone().builder().mergeExternals(extIndexPartition).build();
                    CollapsedHeapConfiguration collapsed = new CollapsedHeapConfiguration(hc, collapsedHc, extIndexPartition);
                    collapsedRulesOfCurrentRule.add(collapsed);
                }
            }
        }
    }

    public int numberOriginalRules() {
        return originalRules.size();
    }

    public int numberCollapsedRules(int originalRuleIdx) {
        return collapsedRules.get(originalRuleIdx).size();
    }

    public Pair<Nonterminal, HeapConfiguration> getOriginalRule(int originalRuleIdx) {
        return originalRules.get(originalRuleIdx);
    }

    public CollapsedHeapConfiguration getCollapsedRhs(int originalRuleIdx, int collapsedRuleIdx) {
        return collapsedRules.get(originalRuleIdx).get(collapsedRuleIdx);
    }

    public String getGrammarName() {
        return grammarName;
    }
}

package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.grammar.canonicalization.CanonicalizationHelper;
import de.rwth.i2.attestor.grammar.canonicalization.CanonicalizationStrategy;
import de.rwth.i2.attestor.grammar.canonicalization.EmbeddingCheckerProvider;
import de.rwth.i2.attestor.grammar.canonicalization.GeneralCanonicalizationStrategy;
import de.rwth.i2.attestor.grammar.canonicalization.defaultGrammar.DefaultCanonicalizationHelper;
import de.rwth.i2.attestor.grammar.util.ExternalNodesPartitioner;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.morphism.MorphismOptions;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

public class NamedGrammar extends Grammar {
    final String grammarName;
    final List<Pair<Nonterminal, HeapConfiguration>> originalRules;
    final List<List<CollapsedHeapConfiguration>> collapsedRules;
    final CanonicalizationStrategy canonicalizationStrategy;

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
        MorphismOptions options = new AbstractionOptions()
                .setAdmissibleAbstraction(false)
                .setAdmissibleConstants(false)
                .setAdmissibleMarkings(false);

        EmbeddingCheckerProvider provider = new EmbeddingCheckerProvider(options);
        CanonicalizationHelper canonicalizationHelper = new DefaultCanonicalizationHelper(provider);
        canonicalizationStrategy = new GeneralCanonicalizationStrategy(this, canonicalizationHelper);
    }

    public int numberOriginalRules() {
        return originalRules.size();
    }

    public int numberCollapsedRules(int originalRuleIdx) {
        return collapsedRules.get(originalRuleIdx).size();
    }

    @Deprecated
    public Pair<Nonterminal, HeapConfiguration> getOriginalRule(int originalRuleIdx) {
        return originalRules.get(originalRuleIdx);
    }

    @Deprecated
    public CollapsedHeapConfiguration getCollapsedRhs(int originalRuleIdx, int collapsedRuleIdx) {
        return collapsedRules.get(originalRuleIdx).get(collapsedRuleIdx);
    }

    public String getGrammarName() {
        return grammarName;
    }

    @Deprecated
    public List<Pair<Integer, Integer>> getAllRuleIdPairs() {
        List<Pair<Integer, Integer>> individualGrammarRules = new ArrayList<>();
        for (int originalRuleId = 0; originalRuleId < numberOriginalRules(); originalRuleId++) {
            individualGrammarRules.add(new Pair<>(originalRuleId, null));
            for (int collapsedRuleId = 0; collapsedRuleId < numberCollapsedRules(originalRuleId); collapsedRuleId++) {
                individualGrammarRules.add(new Pair<>(originalRuleId, collapsedRuleId));
            }
        }
        return individualGrammarRules;
    }

    @Deprecated
    public Pair<Nonterminal, CollapsedHeapConfiguration> getRule(Pair<Integer, Integer> ruleIds) {
        if (ruleIds.second() == null) {
            Pair<Nonterminal, HeapConfiguration> rule = getOriginalRule(ruleIds.first());
            CollapsedHeapConfiguration collapsedHeapConfiguration = new CollapsedHeapConfiguration(rule.second(), rule.second(), null);
            return new Pair<>(rule.first(), collapsedHeapConfiguration);
        } else {
            Nonterminal nonterminal = getOriginalRule(ruleIds.first()).first();
            return new Pair<>(nonterminal, getCollapsedRhs(ruleIds.first(), ruleIds.second()));
        }
    }

    public CanonicalizationStrategy getCanonicalizationStrategy() {
        return canonicalizationStrategy;
    }


    public Collection<GrammarRule> getGrammarRules() {
        Collection<GrammarRule> result = new ArrayList<>();
        for (int originalRuleIdx = 0; originalRuleIdx < originalRules.size(); originalRuleIdx++) {
            result.add(new GrammarRule(originalRuleIdx));
            List<CollapsedHeapConfiguration> collapsedHeapConfigurations = collapsedRules.get(originalRuleIdx);
            for (int collapsedRuleIdx = 0; collapsedRuleIdx < collapsedHeapConfigurations.size(); collapsedRuleIdx++) {
                result.add(new GrammarRule(originalRuleIdx, collapsedRuleIdx));
            }
        }
        return result;
    }

    public static NamedGrammar getNamedGrammar(Collection<GrammarRule> rules) {
        // TODO
        throw new UnsupportedOperationException();
    }
}

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
import org.jboss.util.Heap;

import java.util.*;

/**
 * A grammar with a name, where each rule is numbered.
 */
public class NamedGrammar extends Grammar {

    /**
     * Helper class to store multiple values in the originalRules map
     */
    private class OriginalRule {
        public OriginalRule(Nonterminal nt, HeapConfiguration hc) {
            this.nonterminal = nt;
            this.heapConfiguration = hc;
            this.collapsedHeapConfigurationMap = new HashMap<>();
        }
        final Nonterminal nonterminal;
        final HeapConfiguration heapConfiguration;
        final Map<Integer, CollapsedHeapConfiguration> collapsedHeapConfigurationMap;
    }

    final private String grammarName;
    final private  Map<Integer, OriginalRule> originalRules;

    final CanonicalizationStrategy canonicalizationStrategy;

    public NamedGrammar(Grammar grammar, String name) {
        super(grammar.rules, grammar.collapsedRules);
        originalRules = new HashMap<>();
        grammarName = name;

        int originalRuleIdx = 0;
        for(Map.Entry<Nonterminal, Set<HeapConfiguration>> entry : rules.entrySet()) {

            Nonterminal nonterminal = entry.getKey();
            boolean[] reductionTentacles = new boolean[nonterminal.getRank()];
            for(int i=0; i < nonterminal.getRank(); i++) {
                reductionTentacles[i] = nonterminal.isReductionTentacle(i);
            }

            for(HeapConfiguration hc : entry.getValue()) {
                // Add original rule
                OriginalRule newOriginalRule = new OriginalRule(nonterminal, hc);
                originalRules.put(originalRuleIdx, newOriginalRule);
                // Add collapsed rules
                int collapsedRuleIdx = 0;
                ExternalNodesPartitioner partitioner = new ExternalNodesPartitioner(hc, reductionTentacles);
                for(TIntArrayList extIndexPartition : partitioner.getPartitions()) {
                    HeapConfiguration collapsedHc = hc.clone().builder().mergeExternals(extIndexPartition).build();
                    CollapsedHeapConfiguration collapsed = new CollapsedHeapConfiguration(hc, collapsedHc, extIndexPartition);
                    newOriginalRule.collapsedHeapConfigurationMap.put(collapsedRuleIdx, collapsed);
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

    @Deprecated
    public int numberCollapsedRules(int originalRuleIdx) {
        return originalRules.get(originalRuleIdx).collapsedHeapConfigurationMap.size();
    }

    @Deprecated
    public Pair<Nonterminal, HeapConfiguration> getOriginalRule(int originalRuleIdx) {
        return new Pair<>(originalRules.get(originalRuleIdx).nonterminal, originalRules.get(originalRuleIdx).heapConfiguration);
    }

    @Deprecated
    public Nonterminal getNonterminal(int originalRuleIdx) {
        return originalRules.get(originalRuleIdx).nonterminal;
    }

    @Deprecated
    public HeapConfiguration getHeapConfiguration(int originalRuleIdx) {
        return originalRules.get(originalRuleIdx).heapConfiguration;
    }

    @Deprecated
    public CollapsedHeapConfiguration getCollapsedRhs(int originalRuleIdx, int collapsedRuleIdx) {
        return originalRules.get(originalRuleIdx).collapsedHeapConfigurationMap.get(collapsedRuleIdx);
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
        for (Map.Entry<Integer, OriginalRule> originalRuleEntry : originalRules.entrySet()) {
            result.add(new NamedGrammarRule(this, originalRuleEntry.getKey()));
            for (Map.Entry<Integer, CollapsedHeapConfiguration> collapsedHCEntry : originalRuleEntry.getValue().collapsedHeapConfigurationMap.entrySet()) {
                result.add(new NamedGrammarRule(this, originalRuleEntry.getKey(), collapsedHCEntry.getKey()));
            }
        }
        return result;
    }

    /**
     * Converts the collection of grammar rules into a new NamedGrammar.
     *
     * @throws IllegalArgumentException NamedGrammarRules should not have conflicting indices (e.g. if they come from multiple named grammars)
     */
    public static NamedGrammar getNamedGrammar(Collection<GrammarRule> rules) {
        // TODO
        throw new UnsupportedOperationException();
    }

}

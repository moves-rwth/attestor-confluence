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
            for (int i=0; i < nonterminal.getRank(); i++) {
                reductionTentacles[i] = nonterminal.isReductionTentacle(i);
            }

            // Add original rules
            for (HeapConfiguration originalHC : entry.getValue()) {
                OriginalRule newOriginalRule = new OriginalRule(nonterminal, originalHC);
                originalRules.put(originalRuleIdx, newOriginalRule);

                // Add collapsed rules
                int collapsedRuleIdx = 0;
                ExternalNodesPartitioner partitioner = new ExternalNodesPartitioner(originalHC, reductionTentacles);
                for (TIntArrayList extIndexPartition : partitioner.getPartitions()) {
                    HeapConfiguration collapsedHc = originalHC.clone().builder().mergeExternals(extIndexPartition).build();
                    CollapsedHeapConfiguration collapsed = new CollapsedHeapConfiguration(originalHC, collapsedHc, extIndexPartition);
                    newOriginalRule.collapsedHeapConfigurationMap.put(collapsedRuleIdx, collapsed);
                    collapsedRuleIdx++;
                }

                originalRuleIdx++;
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

    public CanonicalizationStrategy getCanonicalizationStrategy() {
        return canonicalizationStrategy;
    }


    public Collection<NamedGrammarRule> getAllGrammarRules() {
        Collection<NamedGrammarRule> result = new ArrayList<>();
        for (int originalRuleIdx : new TreeSet<>(originalRules.keySet())) {
            result.add(new NamedGrammarRule(this, originalRuleIdx));
            OriginalRule originalRule = originalRules.get(originalRuleIdx);
            for (int collapsedRuleIdx : new TreeSet<>(originalRule.collapsedHeapConfigurationMap.keySet())) {
                result.add(new NamedGrammarRule(this, originalRuleIdx, collapsedRuleIdx));
            }
        }
        return result;
    }

    public Collection<NamedGrammarRule> getOriginalGrammarRules() {
        Collection<NamedGrammarRule> result = new ArrayList<>();
        for (int originalRuleIdx : new TreeSet<>(originalRules.keySet())) {
            result.add(new NamedGrammarRule(this, originalRuleIdx));
        }
        return result;
    }

    public Collection<NamedGrammarRule> getCollapsedGrammarRules(NamedGrammarRule originalRule) {
        Collection<NamedGrammarRule> result = new ArrayList<>();
        Map<Integer, CollapsedHeapConfiguration> collapsedHeapConfigurationMap = originalRules.get(originalRule.getOriginalRuleIdx()).collapsedHeapConfigurationMap;
        for (int collapsedRuleIdx : new TreeSet<>(collapsedHeapConfigurationMap.keySet())) {
            result.add(new NamedGrammarRule(this, originalRule.getOriginalRuleIdx(), collapsedRuleIdx));
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

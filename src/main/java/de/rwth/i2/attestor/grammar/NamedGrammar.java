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

/**
 * A grammar with a name, where each rule is numbered.
 */
public class NamedGrammar extends Grammar {
    final private String grammarName;
    List<GrammarRuleOriginal> originalRules;  // The rules are ordered by the original rule idx

    final CanonicalizationStrategy canonicalizationStrategy;

    public NamedGrammar(Grammar grammar, String name) {
        super(grammar.rules, grammar.collapsedRules);
        this.grammarName = name;
        this.originalRules = new ArrayList<>();

        for(Map.Entry<Nonterminal, Set<HeapConfiguration>> entry : rules.entrySet()) {
            Nonterminal nonterminal = entry.getKey();
            boolean[] reductionTentacles = new boolean[nonterminal.getRank()];
            for (int i=0; i < nonterminal.getRank(); i++) {
                reductionTentacles[i] = nonterminal.isReductionTentacle(i);
            }

            // Add original rules
            for (HeapConfiguration originalHC : entry.getValue()) {
                int originalRuleIdx = originalRules.size();
                List<GrammarRuleCollapsed> collapsedRules = new ArrayList<>();
                GrammarRuleOriginal originalRule = new GrammarRuleOriginal(this, originalRuleIdx, nonterminal, originalHC, collapsedRules, false);
                originalRules.add(originalRule);

                // Add collapsed rules
                ExternalNodesPartitioner partitioner = new ExternalNodesPartitioner(originalHC, reductionTentacles);
                for (TIntArrayList extIndexPartition : partitioner.getPartitions()) {
                    HeapConfiguration collapsedHc = originalHC.clone().builder().mergeExternals(extIndexPartition).build();
                    CollapsedHeapConfiguration collapsed = new CollapsedHeapConfiguration(originalHC, collapsedHc, extIndexPartition);
                    collapsedRules.add(new GrammarRuleCollapsed(originalRule, collapsedRules.size(), collapsed, false));
                }
            }
        }

        canonicalizationStrategy = createCanonicalizationStrategy();
    }

    private GeneralCanonicalizationStrategy createCanonicalizationStrategy() {
        MorphismOptions options = new AbstractionOptions()
                .setAdmissibleAbstraction(false)
                .setAdmissibleConstants(false)
                .setAdmissibleMarkings(false);

        EmbeddingCheckerProvider provider = new EmbeddingCheckerProvider(options);
        CanonicalizationHelper canonicalizationHelper = new DefaultCanonicalizationHelper(provider);
        return new GeneralCanonicalizationStrategy(this, canonicalizationHelper);
    }

    public String getGrammarName() {
        return grammarName;
    }

    public CanonicalizationStrategy getCanonicalizationStrategy() {
        return canonicalizationStrategy;
    }

    public List<GrammarRule> getAllGrammarRules() {
        List<GrammarRule> result = new ArrayList<>();
        for (GrammarRuleOriginal originalRule : originalRules) {
            result.add(originalRule);
            result.addAll(originalRule.getCollapsedRules());
        }
        return result;
    }

    public Iterable<GrammarRuleOriginal> getOriginalGrammarRules() {
        return originalRules;
    }

    /**
     * Creates a new grammar with the given modifications.
     * @param deactivateRules  Rules that are in this grammar which should be deactivated for abstraction
     * @param activateRules  Rules that are in this grammar which should be activated for abstraction
     * @param addRules  Rules that should be added to this grammar
     * @return the resulting grammar
     */
    public NamedGrammar getModyfiedGrammar(Iterable<GrammarRule> deactivateRules, Iterable<GrammarRule> activateRules, Iterable<GrammarRuleOriginal> addRules) {

        throw new UnsupportedOperationException();
    }

}

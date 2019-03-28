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
 *
 */
public class NamedGrammar {
    final private String grammarName;
    final private Grammar abstractionGrammar, concretizationGrammar;
    final private List<GrammarRuleOriginal> originalRules;  // The rules are ordered by the original rule idx

    final CanonicalizationStrategy canonicalizationStrategy;

    private NamedGrammar(NamedGrammar oldGrammar, List<GrammarRuleOriginal> newOriginalRules) {
        this.concretizationGrammar = oldGrammar.concretizationGrammar;
        this.grammarName = oldGrammar.grammarName;
        this.originalRules = newOriginalRules;
        Map<Nonterminal, Set<HeapConfiguration>> abstractionOriginalRules = new HashMap<>();
        Map<Nonterminal, Set<CollapsedHeapConfiguration>> abstractionCollapsedRules = new HashMap<>();

        for (GrammarRuleOriginal originalRule : originalRules) {
            switch (originalRule.getRuleStatus()) {
                case ACTIVE:
                case CONFLUENCE_GENERATED:
                    Set<HeapConfiguration> originalRules = abstractionOriginalRules.computeIfAbsent(originalRule.getNonterminal(), HashSet::new);
            }
        }
        // TODO: Compute the abstraction rules

        this.abstractionGrammar = new Grammar(abstractionOriginalRules, abstractionCollapsedRules);
        this.canonicalizationStrategy = createCanonicalizationStrategy(abstractionGrammar);
    }

    public NamedGrammar(Grammar grammar, String name) {
        this.abstractionGrammar = grammar;
        this.concretizationGrammar = grammar;
        this.grammarName = name;
        this.originalRules = new ArrayList<>();

        for(Map.Entry<Nonterminal, Set<HeapConfiguration>> entry : grammar.rules.entrySet()) {
            Nonterminal nonterminal = entry.getKey();
            boolean[] reductionTentacles = new boolean[nonterminal.getRank()];
            for (int i=0; i < nonterminal.getRank(); i++) {
                reductionTentacles[i] = nonterminal.isReductionTentacle(i);
            }

            // Add original rules
            for (HeapConfiguration originalHC : entry.getValue()) {
                int originalRuleIdx = originalRules.size();
                List<GrammarRuleCollapsed> collapsedRules = new ArrayList<>();
                GrammarRuleOriginal originalRule = new GrammarRuleOriginal(this, originalRuleIdx, nonterminal, originalHC, collapsedRules, GrammarRule.RuleStatus.ACTIVE);
                originalRules.add(originalRule);

                // Add collapsed rules
                ExternalNodesPartitioner partitioner = new ExternalNodesPartitioner(originalHC, reductionTentacles);
                for (TIntArrayList extIndexPartition : partitioner.getPartitions()) {
                    HeapConfiguration collapsedHc = originalHC.clone().builder().mergeExternals(extIndexPartition).build();
                    CollapsedHeapConfiguration collapsed = new CollapsedHeapConfiguration(originalHC, collapsedHc, extIndexPartition);
                    collapsedRules.add(new GrammarRuleCollapsed(originalRule, collapsedRules.size(), collapsed, GrammarRule.RuleStatus.ACTIVE));
                }
            }
        }

        canonicalizationStrategy = createCanonicalizationStrategy(abstractionGrammar);
    }

    private static GeneralCanonicalizationStrategy createCanonicalizationStrategy(Grammar abstractionGrammar) {
        MorphismOptions options = new AbstractionOptions()
                .setAdmissibleAbstraction(false)
                .setAdmissibleConstants(false)
                .setAdmissibleMarkings(false);

        EmbeddingCheckerProvider provider = new EmbeddingCheckerProvider(options);
        CanonicalizationHelper canonicalizationHelper = new DefaultCanonicalizationHelper(provider);
        return new GeneralCanonicalizationStrategy(abstractionGrammar, canonicalizationHelper);
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

    public Grammar getAbstractionGrammar() {
        return abstractionGrammar;
    }

    public Grammar getConcretizationGrammar() {
        return concretizationGrammar;
    }

    private NamedGrammar(NamedGrammar oldGrammar, Collection<GrammarRule> flipActivation, Iterable<GrammarRuleOriginal> addRules) {
        this.originalRules = new ArrayList<>();
        this.grammarName = oldGrammar.grammarName;

        // Activate / Deactivate rules
        for (GrammarRuleOriginal oldOriginalRule : oldGrammar.originalRules) {
            GrammarRuleOriginal newOriginalRule = oldOriginalRule.changeRuleActivation(this, flipActivation);
            if (newOriginalRule != null) {
                this.originalRules.add(newOriginalRule);
            }
        }

        // Add new rules
        for (GrammarRuleOriginal newOriginalRule : addRules) {
            this.originalRules.add(newOriginalRule.attachToGrammar(this, this.originalRules.size()));
        }

        // TODO: Set concretizationGrammar and abstractionGrammar

        this.canonicalizationStrategy = createCanonicalizationStrategy(abstractionGrammar);
    }

    /**
     * Creates a new grammar with the given modifications. The collections deactivateRules and activateRules should be disjoint.
     *
     * @param flipActivation Rules in the grammar whose activation should be flipped
     * @param addRules  Rules that should be added to this grammar
     * @return the resulting grammar
     */
    public NamedGrammar getModifiedGrammar(Collection<GrammarRule> flipActivation, Iterable<GrammarRuleOriginal> addRules) {
        return new NamedGrammar(this, flipActivation, addRules);
    }

}

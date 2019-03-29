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
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

/**
 * A grammar with a name, where each rule is numbered.
 * TODO: How to integrate with attestor? Maybe extend Grammar class and behave like the concretization grammar
 */
public class NamedGrammar {
    final private String grammarName;
    final private Grammar abstractionGrammar, concretizationGrammar;
    final private List<GrammarRuleOriginal> originalRules;  // The rules are ordered by the original rule idx

    // TODO: Add methods to access / set this attribute
    final private Collection<HeapConfiguration> abstractionBlockingHeapConfigurations;

    final CanonicalizationStrategy canonicalizationStrategy;

    private NamedGrammar(String grammarName, List<GrammarRuleOriginal> newOriginalRules) {
        // Check that the original rule indices are in increasing order TODO: Can we just remove this sanity check?
        int currentOriginalRuleIdx = -1;
        for (GrammarRuleOriginal originalRule : newOriginalRules) {
            if (originalRule.getOriginalRuleIdx() <= currentOriginalRuleIdx) {
                throw new IllegalArgumentException("newOriginalRules are not in increasing order");
            }
            currentOriginalRuleIdx = originalRule.getOriginalRuleIdx();
        }

        this.grammarName = grammarName;
        this.originalRules = newOriginalRules;
        Map<Nonterminal, Set<HeapConfiguration>> abstractionOriginalRules = new HashMap<>();
        Map<Nonterminal, Set<CollapsedHeapConfiguration>> abstractionCollapsedRules = new HashMap<>();

        this.abstractionGrammar = getGrammar(newOriginalRules, true);
        this.concretizationGrammar = getGrammar(newOriginalRules, false);

        this.canonicalizationStrategy = createCanonicalizationStrategy(abstractionGrammar);
        this.abstractionBlockingHeapConfigurations = Collections.emptyList();
    }

    public NamedGrammar(Grammar grammar, String name) {
        this.abstractionGrammar = grammar;
        this.concretizationGrammar = grammar;
        this.grammarName = name;
        this.originalRules = new ArrayList<>();
        this.abstractionBlockingHeapConfigurations = Collections.emptyList();

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
                GrammarRuleOriginal originalRule = new GrammarRuleOriginal(grammarName, originalRuleIdx, nonterminal, originalHC, collapsedRules, GrammarRule.RuleStatus.ACTIVE);
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

        this.canonicalizationStrategy = createCanonicalizationStrategy(abstractionGrammar);
    }

    private static Grammar getGrammar(List<GrammarRuleOriginal> grammarRules, boolean abstractionGrammar) {
        GrammarBuilder resultingGrammar = new GrammarBuilder();
        for (GrammarRuleOriginal originalRule : grammarRules) {
            if (!abstractionGrammar || originalRule.getRuleStatus() == GrammarRule.RuleStatus.ACTIVE || originalRule.getRuleStatus() == GrammarRule.RuleStatus.CONFLUENCE_GENERATED) {
                // Concretization grammars contain all rules, abstraction grammars only active (and confluence generated) rules
                resultingGrammar.addRule(originalRule.getNonterminal(), originalRule.getHeapConfiguration());
            }
            for (GrammarRuleCollapsed collapsedRule : originalRule.getCollapsedRules()) {
                if (!abstractionGrammar || originalRule.getRuleStatus() == GrammarRule.RuleStatus.ACTIVE || originalRule.getRuleStatus() == GrammarRule.RuleStatus.CONFLUENCE_GENERATED) {
                    // Note: There actually should not be any collapsed rules that are confluence generated!
                    resultingGrammar.addCollapsedRule(collapsedRule.getNonterminal(), collapsedRule.getCollapsedHeapConfiguration());
                }
            }
        }
        return resultingGrammar.build();
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

    /**
     * Creates a new grammar with the given modifications. The collections deactivateRules and activateRules should be disjoint.
     *
     * @param flipActivation Rules in the grammar whose activation should be flipped
     * @param addRules  Rules that should be added to this grammar
     * @return the resulting grammar
     */
    public NamedGrammar getModifiedGrammar(Collection<GrammarRule> flipActivation, Iterable<GrammarRuleOriginal> addRules) {
        List<GrammarRuleOriginal> newOriginalRules = new ArrayList<>();

        // Activate / Deactivate rules
        for (GrammarRuleOriginal oldOriginalRule : originalRules) {
            GrammarRuleOriginal newOriginalRule = oldOriginalRule.changeRuleActivation(flipActivation);
            if (newOriginalRule != null) {
                newOriginalRules.add(newOriginalRule);
            }
        }

        // Add new rules
        for (GrammarRuleOriginal newOriginalRule : addRules) {
            newOriginalRules.add(newOriginalRule);
        }

        return new NamedGrammar(grammarName, newOriginalRules);
    }

    public int getMaxOriginalRuleIdx() {
        if (originalRules.isEmpty()) {
            return  -1;
        } else {
            return originalRules.get(originalRules.size() - 1).getOriginalRuleIdx();
        }
    }

}

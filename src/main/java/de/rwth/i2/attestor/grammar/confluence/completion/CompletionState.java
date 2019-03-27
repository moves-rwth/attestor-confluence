package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.GrammarRule;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CompletionState {
    private final Collection<GrammarRule> grammarRules;
    private final Collection<CriticalPair> criticalPairs;
    private final Set<HeapConfiguration> abstractionBlockingSubgraphs;

    /**
     * Initializes an initial completion state for the given grammar
     */
    public CompletionState(NamedGrammar grammar) {
        this.abstractionBlockingSubgraphs = new HashSet<>();
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(grammar);
        this.criticalPairs = criticalPairFinder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE);
        this.grammarRules = grammar.getGrammarRules();
    }

    public CompletionState(Collection<GrammarRule> grammarRules, Collection<CriticalPair> criticalPairs,
                           Set<HeapConfiguration> abstractionBlockingSubgraphs) {
        this.grammarRules = grammarRules;
        this.criticalPairs = criticalPairs;
        this.abstractionBlockingSubgraphs = abstractionBlockingSubgraphs;
    }

    public Collection<GrammarRule> getGrammarRules() {
        return Collections.unmodifiableCollection(grammarRules);
    }

    public Collection<CriticalPair> getCriticalPairs() {
        return Collections.unmodifiableCollection(criticalPairs);
    }

    public Set<HeapConfiguration> getAbstractionBlockingSubgraphs() {
        return Collections.unmodifiableSet(abstractionBlockingSubgraphs);
    }
}

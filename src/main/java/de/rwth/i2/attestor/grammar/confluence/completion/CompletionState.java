package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;

import java.util.*;

public class CompletionState {
    private final NamedGrammar grammar;
    private final Collection<CriticalPair> criticalPairs;

    /**
     * Initializes an initial completion state for the given grammar
     */
    public CompletionState(NamedGrammar grammar) {
        this.grammar = grammar;
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(grammar);
        this.criticalPairs = criticalPairFinder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE);
    }

    public CompletionState(NamedGrammar grammar, Collection<CriticalPair> criticalPairs) {
        this.grammar = grammar;
        this.criticalPairs = criticalPairs;
    }

    public NamedGrammar getGrammar() {
        return grammar;
    }

    public Collection<CriticalPair> getCriticalPairs() {
        return Collections.unmodifiableCollection(criticalPairs);
    }
}

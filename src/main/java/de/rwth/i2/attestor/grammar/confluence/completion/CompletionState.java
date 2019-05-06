package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;
import de.rwth.i2.attestor.grammar.typedness.GrammarTypedness;

import java.util.*;

public class CompletionState {
    private final CompletionState parentState;
    private final NamedGrammar grammar;
    private final Collection<CriticalPair> criticalPairs;  // Does not contain strongly joinable critical pairs
    private final GrammarTypedness types;

    /**
     * Initializes a completion state for the given grammar.
     */
    public CompletionState(NamedGrammar grammar, CompletionState parentState) {
        this.grammar = grammar;
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(grammar);
        this.criticalPairs = criticalPairFinder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE);
        this.parentState = parentState;
        this.types = new GrammarTypedness(grammar.getConcretizationGrammar());
    }

    /**
     * @param criticalPairs  Must not include strongly joinable critical pairs
     */
    public CompletionState(NamedGrammar grammar, Collection<CriticalPair> criticalPairs, CompletionState parentState) {
        this.grammar = grammar;
        this.criticalPairs = criticalPairs;
        this.types = new GrammarTypedness(grammar.getConcretizationGrammar());
        this.parentState = parentState;
    }

    public NamedGrammar getGrammar() {
        return grammar;
    }

    public Collection<CriticalPair> getCriticalPairs() {
        return Collections.unmodifiableCollection(criticalPairs);
    }

    public GrammarTypedness getTypes() {
        return types;
    }

    public CompletionState getParentState() {
        return parentState;
    }
}

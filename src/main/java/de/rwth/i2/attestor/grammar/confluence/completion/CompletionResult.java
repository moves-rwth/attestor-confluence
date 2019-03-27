package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Collection;
import java.util.Set;

public class CompletionResult {
    NamedGrammar grammarResult;
    Set<HeapConfiguration> abstractionBlockingSubgraphs;
    Collection<CriticalPair> problematicCriticalPairs;

    CompletionResult(NamedGrammar grammarResult, Set<HeapConfiguration> abstractionBlockingSubgraphs, Collection<CriticalPair> problematicCriticalPairs) {
        this.grammarResult = grammarResult;
        this.abstractionBlockingSubgraphs = abstractionBlockingSubgraphs;
        this.problematicCriticalPairs = problematicCriticalPairs;
    }

    /**
     *
     * @return The resulting grammar after executing the completion procedure (Important: The grammar can still be not confluent)
     */
    NamedGrammar getGrammarResult() {
        return grammarResult;
    }

    /**
     * @return A set of heap configurations that block further abstraction when encountered during attestors canonicalization.
     */
    Set<HeapConfiguration> getAbstractionBlockingSubgraphs() {
        return abstractionBlockingSubgraphs;
    }


    /**
     * @return Critical Pairs that cannot be resolved using the given completion heuristics
     */
    Collection<CriticalPair> getProplematicCriticalPairs() {
        return problematicCriticalPairs;
    }
}

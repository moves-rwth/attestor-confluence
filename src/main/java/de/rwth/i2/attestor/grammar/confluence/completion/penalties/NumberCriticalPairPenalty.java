package de.rwth.i2.attestor.grammar.confluence.completion.penalties;

import de.rwth.i2.attestor.grammar.GrammarRule;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Collection;
import java.util.Set;

public class NumberCriticalPairPenalty implements CompletionStatePenalty {
    @Override
    public int getPenalty(Collection<CriticalPair> criticalPairs, Collection<GrammarRule> rules, Set<HeapConfiguration> abstractionBlockingHeapConfigurations) {
        return criticalPairs.size();
    }
}

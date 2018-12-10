package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.Grammar;

import java.util.HashSet;
import java.util.Set;


/**
 *
 * Computes the critical pairs of a grammar on construction.
 * Provides methods to access information about the critical pairs.
 *
 * @author Johannes Schulte
 */
public class CriticalPairs {

    final Grammar underlyingGrammar;
    final Set<CriticalPair> criticalPairs;

    public CriticalPairs(Grammar grammar) {
        // TODO: Object creation invokes computation of critical pairs (should this be triggered by another method?)
        this.underlyingGrammar = grammar;
        // TODO: Calculate critical pairs
        this.criticalPairs = new HashSet<>();
    }

}

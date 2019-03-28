package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

public interface GrammarRule {

    boolean isOriginalRule();

    /**
     * @return true if this rule should only be used for concretization and not for abstraction
     */
    boolean deactivatedForAbstraction();

    Nonterminal getNonterminal();

    HeapConfiguration getHeapConfiguration();

    /**
     * Can be called for collapsed and non collapsed rules. For non collapsed rules, the morphism in the collapsed heap configuration is null.
     */
    CollapsedHeapConfiguration getCollapsedHeapConfiguration();
}

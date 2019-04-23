package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Collection;

public interface GrammarRule {

    enum RuleStatus {
        ACTIVE,  // The rule originates from a handwritten rule and can be used for abstraction
        INACTIVE,  // The rule originates from a handwritten rule and cannot be used for abstraction
        CONFLUENCE_GENERATED  // The rule was generated to achieve confluence and can be used for abstraction (those rules are never deactivated, but just deleted)
    }

    /**
     * @return true if this rule should only be used for concretization and not for abstraction
     */
    RuleStatus getRuleStatus();

    Nonterminal getNonterminal();

    HeapConfiguration getHeapConfiguration();

    /**
     * Can be called for collapsed and non collapsed rules. For non collapsed rules, the morphism in the collapsed heap configuration is null.
     */
    CollapsedHeapConfiguration getCollapsedHeapConfiguration();

    String getGrammarName();

    int getOriginalRuleIdx();

    default boolean isRuleActive() {
        switch (getRuleStatus()) {
            case ACTIVE:
            case CONFLUENCE_GENERATED:
                return true;
            case INACTIVE:
                return false;
        }
        throw new IllegalStateException();
    }

}

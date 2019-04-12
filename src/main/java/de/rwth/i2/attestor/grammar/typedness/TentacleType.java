package de.rwth.i2.attestor.grammar.typedness;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import gnu.trove.list.array.TIntArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A class to express that the type of one tentacle
 */
public class TentacleType {
    private final GrammarTypedness grammarTypedness;
    private final Nonterminal nonterminal;
    private final int tentacle;
    private final Set<TentacleType> unterminatedDependencies;  // This type depends on those types which have not yet been fully calculated
    private final Set<SelectorLabel> immediateReachable;  // All types that can get generated through a single rule application
    private final Set<SelectorLabel> allTypes;
    private boolean calculationInProgress;  // Used to resolve recursive dependency loops


    public TentacleType(GrammarTypedness grammarTypedness, Nonterminal nt, int tentacle) {
        this.calculationInProgress = false;
        this.grammarTypedness = grammarTypedness;
        this.nonterminal = nt;
        this.tentacle = tentacle;
        this.immediateReachable = new HashSet<>();
        this.unterminatedDependencies = new HashSet<>();

        // Initialize direct types and dependencies
        Grammar grammar = grammarTypedness.getGrammar();

        for (HeapConfiguration rhs : grammar.getRightHandSidesFor(nt)) {
            int node = rhs.externalNodeAt(tentacle);
            immediateReachable.addAll(rhs.selectorLabelsOf(node));
            rhs.attachedNonterminalEdgesOf(node).forEach(ntEdge-> {
                Nonterminal ntDependency = rhs.labelOf(ntEdge);
                TIntArrayList attachedNodes = rhs.attachedNodesOf(ntEdge);
                int offset = 0;
                while (offset != -1) {
                    offset = attachedNodes.indexOf(offset, node);
                    if (offset != -1) {
                        // attachedNodes[offset] == node
                        unterminatedDependencies.add(grammarTypedness.getTentacleType(nt, offset));
                        offset++;  // Search following indices
                    }
                }
                return true;
            });
        }

        this.allTypes = new HashSet<>(immediateReachable);
    }

    public Set<SelectorLabel> getImmediateReachable() {
        return immediateReachable;
    }

    public Set<SelectorLabel> getAllCurrentTypes() {
        return allTypes;
    }

    /**
     * @return set of new types at tentacle
     */
    public Set<TentacleType> recalculateDependencies() {
        if (calculationInProgress) {
            // Don't start another re-computation to ensure termination
            return null;
        } else {
            calculationInProgress = true;

            for (TentacleType dependency : unterminatedDependencies) {

            }


            calculationInProgress = false;
        }
    }

}
